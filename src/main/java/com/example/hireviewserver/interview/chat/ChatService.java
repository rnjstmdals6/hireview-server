package com.example.hireviewserver.interview.chat;

import com.example.hireviewserver.gemini.GeminiService;
import com.example.hireviewserver.gemini.GeminiStructuredRequestDTO;
import com.example.hireviewserver.interview.question.Question;
import com.example.hireviewserver.interview.question.QuestionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SessionQuestionRepository sessionQuestionRepository;
    private final QuestionRepository questionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GeminiService geminiService;
    private final Map<String, SessionSinkData> sessionSinkMap = new ConcurrentHashMap<>();

    private SessionSinkData getOrCreateSessionSinkData(String sessionId) {
        return sessionSinkMap.computeIfAbsent(sessionId, key -> new SessionSinkData());
    }

    public void completeSession(String sessionId) {
        SessionSinkData data = sessionSinkMap.remove(sessionId);
        if (data != null) {
            data.getSink().tryEmitComplete();
        }
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupOldSessions() {
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, SessionSinkData> entry : sessionSinkMap.entrySet()) {
            String sessionId = entry.getKey();
            SessionSinkData data = entry.getValue();
            Duration diff = Duration.between(data.getLastAccessTime(), now);
            if (diff.toMinutes() >= 30) {
                completeSession(sessionId);
            }
        }
    }

    public Mono<ChatMessage> saveMessage(String sessionId, String sender, String content) {
        ChatMessage chatMessage = new ChatMessage(sessionId, sender, content);
        return chatMessageRepository.save(chatMessage)
                .doOnNext(savedMsg -> {
                    SessionSinkData data = getOrCreateSessionSinkData(sessionId);
                    data.updateLastAccessTime();
                    data.getSink().tryEmitNext(savedMsg);
                });
    }

    public Flux<ChatMessage> streamLiveMessages(String sessionId) {
        SessionSinkData data = getOrCreateSessionSinkData(sessionId);
        data.updateLastAccessTime();
        return data.getSink().asFlux();
    }

    public Flux<ChatMessage> getMessages(String sessionId) {
        return chatMessageRepository.findBySessionId(sessionId);
    }

    public Flux<ChatMessage> getNextQuestion(String sessionId, int step) {
        if (step >= 5) {
            return generateFinalSummary(sessionId);
        }
        return sessionQuestionRepository.findBySessionIdAndStep(sessionId, step)
                .flatMap(sq -> questionRepository.findById(sq.getQuestionId()))
                .flatMap(question -> {
                    ChatMessage questionMessage = new ChatMessage(sessionId, "AI", question.getContent());
                    questionMessage.setQuestion(true);
                    return chatMessageRepository.save(questionMessage)
                            .doOnNext(msg -> {
                                SessionSinkData data = getOrCreateSessionSinkData(sessionId);
                                data.updateLastAccessTime();
                                data.getSink().tryEmitNext(msg);
                            });
                })
                .flux();
    }

    private Flux<ChatMessage> generateFinalSummary(String sessionId) {
        return chatMessageRepository.findBySessionId(sessionId).collectList()
                .flatMapMany(allMessages -> {
                    String combined = buildConversation(allMessages);
                    GeminiStructuredRequestDTO request = buildGeminiFinalSummaryRequest(combined);
                    return geminiService.generateStructuredResponse(request)
                            .flatMapMany(rawJson -> {
                                List<String> parsed = parseFinalSummary(rawJson);
                                if (parsed.size() < 3) {
                                    while (parsed.size() < 3) {
                                        parsed.add("Not enough feedback to display.");
                                    }
                                }
                                return Flux.concat(
                                        saveMessage(sessionId, "AI", parsed.get(0)),
                                        saveMessage(sessionId, "AI", parsed.get(1)),
                                        saveMessage(sessionId, "AI", parsed.get(2)),
                                        saveMessage(sessionId, "AI", "The interview has ended.")
                                );
                            });
                });
    }

    private String buildConversation(List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : messages) {
            sb.append(msg.getSender()).append(": ").append(msg.getContent()).append("\n");
        }
        return sb.toString().trim();
    }

    private GeminiStructuredRequestDTO buildGeminiFinalSummaryRequest(String conversation) {
        return GeminiStructuredRequestDTO.builder()
                .contents(List.of(
                        GeminiStructuredRequestDTO.Content.builder()
                                .role("user")
                                .parts(List.of(
                                        GeminiStructuredRequestDTO.Content.Part.builder()
                                                .text(
                                                        "Here is the entire interview conversation:\n\n" +
                                                        conversation + "\n\n" +
                                                        "Please summarize the conversation into 3 pieces of feedback, each 2~3 sentences, in JSON format. " +
                                                        "Only return a JSON object containing a single array field called 'feedbacks' with up to three feedback strings. " +
                                                        "Do not return any extra text besides the JSON."
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
                                                        "feedbacks", GeminiStructuredRequestDTO.ResponseSchemaProperty.builder()
                                                                .type("ARRAY")
                                                                .items(
                                                                        GeminiStructuredRequestDTO.ResponseSchemaProperty.builder()
                                                                                .type("STRING")
                                                                                .build()
                                                                )
                                                                .build()
                                                ))
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    private List<String> parseFinalSummary(String rawJson) {
        List<String> result = new ArrayList<>();
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
                    JsonNode feedbackJson = mapper.readTree(sb.toString().trim());
                    JsonNode array = feedbackJson.path("feedbacks");
                    if (array.isArray()) {
                        for (JsonNode node : array) {
                            result.add(node.asText());
                        }
                    }
                }
            }
        } catch (Exception e) {
            result.add("An error occurred while parsing the final feedback.");
        }
        return result;
    }

    public Flux<ChatMessage> processUserResponse(String sessionId, String userAnswer, int step) {
        return saveMessage(sessionId, "User", userAnswer)
                .flatMapMany(userMsg ->
                        getQuestionText(sessionId, step)
                                .defaultIfEmpty("The interview has ended.")
                                .flatMap(question -> {
                                    if (step >= 5) {
                                        return Mono.just(List.of(userMsg));
                                    }
                                    GeminiStructuredRequestDTO requestDTO = buildGeminiFeedbackRequest(question, userAnswer);
                                    return geminiService.generateStructuredResponse(requestDTO)
                                            .flatMap(rawJson -> {
                                                ParseResult parseResult = parseFeedbackAndPassFromJson(rawJson);
                                                String systemText = parseResult.pass ? "Pass" : "Fail";
                                                String feedbackText = parseResult.feedback;
                                                return Flux.concat(
                                                        saveMessage(sessionId, "System", systemText),
                                                        saveMessage(sessionId, "AI", feedbackText),
                                                        getNextQuestion(sessionId, step + 1)
                                                ).collectList();
                                            })
                                            .map(list -> {
                                                List<ChatMessage> all = new ArrayList<>();
                                                all.add(userMsg);
                                                all.addAll(list);
                                                return all;
                                            });
                                })
                                .flatMapMany(Flux::fromIterable)
                );
    }

    private Mono<String> getQuestionText(String sessionId, int step) {
        return sessionQuestionRepository.findBySessionIdAndStep(sessionId, step)
                .flatMap(sessionQuestion -> questionRepository.findById(sessionQuestion.getQuestionId()))
                .map(Question::getContent);
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
                                                        "User Answer: \"" + userAnswer + "\"\n" +
                                                        "Evaluate the correctness of the user's answer. " +
                                                        "Return a JSON object with two fields:\n" +
                                                        "1) feedback (STRING) - a short reason in 2~3 sentences\n" +
                                                        "2) pass (BOOLEAN)\n" +
                                                        "Return no extra text besides the JSON."
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
                    JsonNode feedbackJson = mapper.readTree(sb.toString().trim());
                    result.feedback = feedbackJson.path("feedback").asText(result.feedback);
                    result.pass = feedbackJson.path("pass").asBoolean(false);
                }
            }
        } catch (Exception e) {
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
