package com.example.hireviewserver.interview.chat;

import com.example.hireviewserver.gemini.GeminiService;
import com.example.hireviewserver.gemini.GeminiStructuredRequestDTO;
import com.example.hireviewserver.interview.question.Question;
import com.example.hireviewserver.interview.question.QuestionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SessionQuestionRepository sessionQuestionRepository;
    private final QuestionRepository questionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GeminiService geminiService;
    private final Sinks.Many<ChatMessage> messageSink =
            Sinks.many().multicast().onBackpressureBuffer(10, false);

    public Mono<ChatMessage> saveMessage(String sessionId, String sender, String content) {
        return chatMessageRepository.save(new ChatMessage(sessionId, sender, content))
                .doOnNext(messageSink::tryEmitNext);
    }

    public Flux<ChatMessage> streamLiveMessages(String sessionId) {
        return messageSink.asFlux()
                .filter(msg -> msg.getSessionId().equals(sessionId));
    }

    public Flux<ChatMessage> getMessages(String sessionId) {
        return chatMessageRepository.findBySessionId(sessionId);
    }

    public Mono<ChatMessage> getNextQuestion(String sessionId, int step) {
        if (step >= 5) {
            return saveMessage(sessionId, "AI", "The interview has ended.");
        }
        return sessionQuestionRepository.findBySessionIdAndStep(sessionId, step)
                .flatMap(sessionQuestion -> questionRepository.findById(sessionQuestion.getQuestionId()))
                .flatMap(question -> {
                    ChatMessage questionMessage = new ChatMessage(sessionId, "AI", question.getContent());
                    questionMessage.setQuestion(true);
                    return chatMessageRepository.save(questionMessage)
                            .doOnNext(messageSink::tryEmitNext);
                });
    }


    private Mono<String> getQuestionText(String sessionId, int step) {
        return sessionQuestionRepository.findBySessionIdAndStep(sessionId, step)
                .flatMap(sessionQuestion -> questionRepository.findById(sessionQuestion.getQuestionId()))
                .map(Question::getContent);
    }

    public Flux<ChatMessage> processUserResponse(String sessionId, String userAnswer, int step) {
        return saveMessage(sessionId, "User", userAnswer)
                .flatMapMany(userMsg ->
                        getQuestionText(sessionId, step)
                                .defaultIfEmpty("The interview has ended.")
                                .flatMap(question -> {
                                    GeminiStructuredRequestDTO requestDTO = buildGeminiFeedbackRequest(question, userAnswer);

                                    return geminiService.generateStructuredResponse(requestDTO)
                                            .flatMap(rawJson -> {
                                                ParseResult parseResult = parseFeedbackAndPassFromJson(rawJson);
                                                String systemText = parseResult.pass ? "Pass" : "Fail";
                                                String feedbackText = parseResult.feedback;

                                                return Flux.concat(
                                                        saveMessage(sessionId, "System", systemText),
                                                        saveMessage(sessionId, "AI", feedbackText),
                                                        getNextQuestion(sessionId, step + 1).flux()
                                                ).collectList();
                                            });
                                })
                                .flatMapMany(Flux::fromIterable)
                                .startWith(userMsg)
                );
    }


    private GeminiStructuredRequestDTO buildGeminiFeedbackRequest(String question, String userAnswer) {
        return GeminiStructuredRequestDTO.builder()
                .contents(List.of(
                        GeminiStructuredRequestDTO.Content.builder()
                                .role("user")
                                .parts(List.of(
                                        GeminiStructuredRequestDTO.Content.Part.builder()
                                                .text(
                                                        "Question: \"" + question + "\"\n" +
                                                        "User Answer: \"" + userAnswer + "\"\n\n" +
                                                        "Evaluate correctness in JSON with 2 fields:\n" +
                                                        "1) feedback (STRING) - short reason (2~3 sentences)\n" +
                                                        "2) pass (BOOLEAN)\n" +
                                                        "No extra text; Return only JSON."
                                                )
                                                .build()
                                ))
                                .build()
                ))
                .generationConfig(
                        GeminiStructuredRequestDTO.GenerationConfig.builder()
                                .response_mime_type("application/json")
                                .response_schema(
                                        GeminiStructuredRequestDTO.ResponseSchema.builder()
                                                .type("OBJECT")
                                                .properties(Map.of(
                                                        "feedback", GeminiStructuredRequestDTO.ResponseSchemaProperty.builder()
                                                                .type("STRING")
                                                                .build(),
                                                        "pass", GeminiStructuredRequestDTO.ResponseSchemaProperty.builder()
                                                                .type("BOOLEAN")
                                                                .build()
                                                ))
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    private static class ParseResult {
        String feedback;
        boolean pass;
    }

    private ParseResult parseFeedbackAndPassFromJson(String rawJson) {
        ParseResult result = new ParseResult();
        result.feedback = "Could not parse feedback from Gemini.";
        result.pass = false;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);

            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode part : parts) {
                        sb.append(part.path("text").asText());
                    }
                    String embeddedJson = sb.toString().trim();

                    JsonNode feedbackJson = mapper.readTree(embeddedJson);
                    result.feedback = feedbackJson.path("feedback").asText(result.feedback);
                    result.pass = feedbackJson.path("pass").asBoolean(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Mono<Void> loadQuestionsForSession(String sessionId, Long jobId) {
        return questionRepository.findRandomQuestionsByJobId(jobId)
                .index()
                .flatMap(indexedQuestion -> {
                    int step = indexedQuestion.getT1().intValue();
                    Question question = indexedQuestion.getT2();
                    return sessionQuestionRepository.save(new SessionQuestion(null, sessionId, question.getId(), step));
                })
                .then();
    }
}
