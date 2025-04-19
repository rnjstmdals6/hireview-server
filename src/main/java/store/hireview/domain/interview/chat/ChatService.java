package store.hireview.domain.interview.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import store.hireview.external.gemini.GeminiGateway;
import store.hireview.external.gemini.GeminiStructuredRequestDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SessionQuestionRepository sessionQuestionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final GeminiGateway geminiGateway;
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

    // 30분 이상 SSE 연결 없으면 세션 정리
    @Scheduled(fixedRate = 300_000) // 5분마다 체크
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

    // chatMessages 테이블에 메시지 저장 + SSE로 실시간 전송
    public Mono<ChatMessage> saveMessage(String sessionId, String sender, String content) {
        ChatMessage chatMessage = new ChatMessage(sessionId, sender, content);
        return chatMessageRepository.save(chatMessage)
                .doOnNext(savedMsg -> {
                    SessionSinkData data = getOrCreateSessionSinkData(sessionId);
                    data.updateLastAccessTime();
                    data.getSink().tryEmitNext(savedMsg);
                });
    }

    public Mono<ChatMessage> saveMessage(ChatMessage message) {
        return chatMessageRepository.save(message)
                .doOnNext(savedMsg -> {
                    SessionSinkData data = getOrCreateSessionSinkData(message.getSessionId());
                    data.updateLastAccessTime();
                    data.getSink().tryEmitNext(savedMsg);
                });
    }

    // SSE 구독용
    public Flux<ChatMessage> streamLiveMessages(String sessionId) {
        SessionSinkData data = getOrCreateSessionSinkData(sessionId);
        data.updateLastAccessTime();

        Flux<ChatMessage> heartbeat = Flux.interval(Duration.ofSeconds(15))
                .map(tick -> new ChatMessage(sessionId, "ping", ":keepalive"))
                .doOnNext(msg -> data.getSink().tryEmitNext(msg));

        return Flux.merge(data.getSink().asFlux(), heartbeat);
    }

    // 히스토리 조회
    public Flux<ChatMessage> getMessages(String sessionId) {
        return chatMessageRepository.findBySessionId(sessionId);
    }

    public Mono<Void> loadQuestionsForSession(String sessionId, CreateSessionRequestDTO req) {
        return geminiGateway.generateStructuredResponse(buildGeminiRequest(req))
                .flatMapMany(json -> {
                    List<String> questions = parseQuestionsFromJson(json);
                    return Flux.fromIterable(questions)
                            .index()
                            .flatMap(indexed -> {
                                long idx = indexed.getT1();
                                String questionText = indexed.getT2();
                                SessionQuestion sq = SessionQuestion.create(
                                        sessionId,
                                        questionText,
                                        (int) idx
                                );
                                return sessionQuestionRepository.save(sq);
                            });
                })
                .then();
    }

    public GeminiStructuredRequestDTO buildGeminiRequest(CreateSessionRequestDTO req) {
        StringBuilder prompt = new StringBuilder();
        if (req.isEnableIceBreaking()) {
            prompt.append("Start with one friendly greeting to ease tension'\n");
        }
        prompt.append("Then provide 5 interview questions.\n");
        prompt.append("JobTitle: ").append(req.getJobTitle()).append("\n");
        prompt.append("Career: ").append(req.getCareer()).append("\n");
        prompt.append("Stack: ").append(req.getStack()).append("\n");
        prompt.append("The interview should be conducted in ").append(req.getLanguage()).append(".\n");
        prompt.append("Additional request: ").append(req.getRequestText()).append("\n");
        prompt.append("Return them as a JSON array of objects, e.g. [{\"question\": \"...\"}].\n");
        prompt.append("Do not include any extra text.");

        GeminiStructuredRequestDTO.ResponseSchemaProperty questionProperty =
                GeminiStructuredRequestDTO.ResponseSchemaProperty.builder()
                        .type("STRING")
                        .build();

        GeminiStructuredRequestDTO.ResponseSchemaItems items =
                GeminiStructuredRequestDTO.ResponseSchemaItems.builder()
                        .type("OBJECT")
                        .properties(Map.of("question", questionProperty))
                        .build();

        return GeminiStructuredRequestDTO.builder()
                .contents(List.of(
                        GeminiStructuredRequestDTO.Content.builder()
                                .role("user")
                                .parts(List.of(
                                        GeminiStructuredRequestDTO.Content.Part.builder()
                                                .text("You are an interview question generator. Output only valid JSON array of objects.")
                                                .build()
                                ))
                                .build(),
                        GeminiStructuredRequestDTO.Content.builder()
                                .role("user")
                                .parts(List.of(
                                        GeminiStructuredRequestDTO.Content.Part.builder()
                                                .text(prompt.toString())
                                                .build()
                                ))
                                .build()
                ))
                .generationConfig(
                        GeminiStructuredRequestDTO.GenerationConfig.builder()
                                .responseMimeType("application/json")
                                .responseSchema(
                                        GeminiStructuredRequestDTO.ResponseSchema.builder()
                                                .type("ARRAY")
                                                .items(items)
                                                .properties(Map.of())
                                                .build()
                                )
                                .build()
                )
                .build();
    }



    // 응답 parsing
    private List<String> parseQuestionsFromJson(String rawJson) {
        List<String> questions = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String innerJson = parts.get(0).path("text").asText();
                    JsonNode array = mapper.readTree(innerJson); // stringified JSON 배열 파싱
                    if (array.isArray()) {
                        for (JsonNode node : array) {
                            questions.add(node.path("question").asText());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return questions;
    }


    public Flux<ChatMessage> getNextQuestion(String sessionId, int step) {
        if (step >= 5) {
            return generateFinalSummary(sessionId);
        }
        return sessionQuestionRepository.findBySessionIdAndStep(sessionId, step)
                .flatMap(sq -> {
                    ChatMessage questionMessage = new ChatMessage(sessionId, "AI", sq.getQuestionText());
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

    // ▼▼▼ 사용자 응답 처리 + 피드백 + 다음 질문 ▼▼▼
    public Flux<ChatMessage> processUserResponse(String sessionId, String userAnswer, int step) {
        // (1) 인터뷰 세션 정보 조회해서 language 가져오기
        return interviewSessionRepository.findBySessionId(sessionId)
                .flatMapMany(session -> {
                    String language = session.getLanguage(); // DB에 저장된 언어

                    if (session.getSessionType() == SessionType.INTERVIEWER) {
                        // ▼ 인터뷰어 모드 ▼
                        return runInterviewerFlow(session, userAnswer);
                    }

                    // (2) 기존 로직 그대로 이어가기
                    return saveMessage(sessionId, "User", userAnswer)
                            .flatMapMany(userMsg ->
                                    getQuestionText(sessionId, step)
                                            .defaultIfEmpty("No more questions.")
                                            .flatMap(question -> {
                                                if (step >= 5) {
                                                    return Mono.just(List.of(userMsg));
                                                }
                                                GeminiStructuredRequestDTO requestDTO =
                                                        buildGeminiFeedbackRequest(question, userAnswer, language);
                                                return geminiGateway.generateStructuredResponse(requestDTO)
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
                });
    }

    private Mono<String> getQuestionText(String sessionId, int step) {
        return sessionQuestionRepository.findBySessionIdAndStep(sessionId, step)
                .map(SessionQuestion::getQuestionText);
    }

    // ▼▼▼ 면접 끝나면 최종 요약 ▼▼▼
    private Flux<ChatMessage> generateFinalSummary(String sessionId) {
        return interviewSessionRepository.findBySessionId(sessionId)
                .flatMapMany(session ->
                        chatMessageRepository.findBySessionId(sessionId).collectList()
                                .flatMapMany(allMessages -> {
                                    String combined = buildConversation(allMessages);
                                    GeminiStructuredRequestDTO request = buildGeminiFinalSummaryRequest(combined, session.getLanguage());
                                    return geminiGateway.generateStructuredResponse(request)
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
                                                        saveMessage(sessionId, "AI", getEndMessage(session.getLanguage()))
                                                );
                                            });
                                })
                );
    }

    private String buildConversation(List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : messages) {
            sb.append(msg.getSender()).append(": ").append(msg.getContent()).append("\n");
        }
        return sb.toString().trim();
    }

    private GeminiStructuredRequestDTO buildGeminiFinalSummaryRequest(String conversation, String language) {
        return GeminiStructuredRequestDTO.builder()
                .contents(List.of(
                        GeminiStructuredRequestDTO.Content.builder()
                                .role("user")
                                .parts(List.of(
                                        GeminiStructuredRequestDTO.Content.Part.builder()
                                                .text(
                                                        "You are a professional interviewer.\n" +
                                                        "Evaluate the following answer and give brief, kind feedback like a real interview coach.\n" +
                                                        "Here is the entire interview conversation:\n\n" +
                                                        conversation + "\n\n" +
                                                        "Please summarize the conversation into 3 pieces of feedback, each 1~2 sentences. " +
                                                        "The feedback must be written in " + language + ". " +
                                                        "Return only a JSON object containing a single array field called 'feedbacks' with up to three strings. " +
                                                        "Do not return any extra text besides the JSON."
                                                )
                                                .build()
                                ))
                                .build()
                ))
                .generationConfig(
                        GeminiStructuredRequestDTO.GenerationConfig.builder()
                                .responseMimeType("application/json")
                                .responseSchema(
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
            // gemini "candidates" parsing
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

    // ▼▼▼ 사용자 답변에 대한 간단한 Pass/Fail + Feedback ▼▼▼
    private GeminiStructuredRequestDTO buildGeminiFeedbackRequest(String question, String userAnswer, String language) {
        return GeminiStructuredRequestDTO.builder()
                .contents(List.of(
                        GeminiStructuredRequestDTO.Content.builder()
                                .role("user")
                                .parts(List.of(
                                        GeminiStructuredRequestDTO.Content.Part.builder()
                                                .text(
                                                        "You are acting as a professional interviewer.\n" +
                                                        "Question: \"" + question + "\"\n" +
                                                        "User Answer: \"" + userAnswer + "\"\n" +
                                                        "Evaluate the correctness of the user's answer.\n" +
                                                        "Return a JSON object with two fields:\n" +
                                                        "1) feedback (STRING) - a short reason in 2~3 sentences\n" +
                                                        "2) pass (BOOLEAN)\n" +
                                                        "The feedback must be written in " + language + ".\n" +
                                                        "Use a friendly and natural tone, as if you are talking to the candidate.\n" +
                                                        "Return only the JSON. Do not add any other text."
                                                )
                                                .build()
                                ))
                                .build()
                ))
                .generationConfig(
                        GeminiStructuredRequestDTO.GenerationConfig.builder()
                                .responseMimeType("application/json")
                                .responseSchema(
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

    private Flux<ChatMessage> runInterviewerFlow(
            InterviewSession s, String qText) {

        String id = s.getSessionId();

        return saveMessage(id, "User", qText)
                .flatMapMany(qMsg ->
                        buildContextSnippet(id, 6)
                                .flatMapMany(ctx -> {
                                    GeminiStructuredRequestDTO req =
                                            buildGeminiCandidateAnswerRequest(
                                                    qText, s.getLanguage(),
                                                    s.getJobTitle(), s.getCareer(),
                                                    s.getStack(), s.getRequestText(),
                                                    ctx);

                                    return geminiGateway.generateStructuredResponse(req)
                                            .map(this::parseAnswerList)
                                            .flatMapMany(list -> {
                                                Mono<ChatMessage> first =
                                                        saveMessage(id, "AI", list.get(0));

                                                ChatMessage secondMsg =
                                                        new ChatMessage(id, "AI", list.get(1));
                                                secondMsg.setQuestion(true);
                                                Mono<ChatMessage> second = saveMessage(secondMsg);

                                                return Flux.concat(first, second);
                                            })
                                            .startWith(qMsg);
                                }));
    }

    private List<String> parseAnswerList(String rawJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson)
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text");          // stringified JSON

            JsonNode arr = mapper.readTree(root.asText());
            List<String> out = new ArrayList<>();
            arr.forEach(node -> out.add(node.path("text").asText()));
            return out;
        } catch (Exception e) {
            return List.of("죄송합니다. 답변 생성에 실패했습니다.");
        }
    }

    private GeminiStructuredRequestDTO buildGeminiCandidateAnswerRequest(
            String question,
            String language,
            String jobTitle,
            String career,
            String stack,
            String requestText,
            String recentContext    // ⬅ 추가
    ) {

        StringBuilder sysPrompt = new StringBuilder()
                .append("You are the candidate in a live job interview.\n")
                .append("- Job Title: ").append(jobTitle).append('\n')
                .append("- Career Level: ").append(career).append('\n')
                .append("- Main Tech Stack: ").append(stack).append('\n');

        if (requestText != null && !requestText.isBlank()) {
            sysPrompt.append("- Additional context: ").append(requestText.trim()).append('\n');
        }

        if (!recentContext.isBlank()) {
            sysPrompt.append("\nRecent dialogue:\n")
                    .append(recentContext)
                    .append("\n\n");
        }

        sysPrompt.append("Answer in ").append(language).append(".\n")
                .append("Respond NATURALLY, like chat. ")
                .append("Split your reply into exactly two short chat bubbles ")
                .append("(each ≤ 100 words). ")
                .append("Return ONLY a JSON array, e.g. ")
                .append("[{\"text\":\"...\"},{\"text\":\"...\"}]. ");

        return GeminiStructuredRequestDTO.builder()
                .contents(List.of(
                        GeminiStructuredRequestDTO.Content.builder()
                                .role("user")
                                .parts(List.of(
                                        GeminiStructuredRequestDTO.Content.Part.builder()
                                                .text(sysPrompt.toString() +
                                                      "\nInterviewer: \"" + question + "\"")
                                                .build()))
                                .build()))
                .generationConfig(
                        GeminiStructuredRequestDTO.GenerationConfig.builder()
                                .responseMimeType("application/json")
                                .build())
                .build();
    }

    private Mono<String> buildContextSnippet(String sessionId, int limit) {
        return chatMessageRepository.findBySessionIdOrderByIdDesc(sessionId)
                .take(limit)
                .collectList()
                .map(list -> {
                    Collections.reverse(list);
                    StringBuilder sb = new StringBuilder();
                    list.forEach(msg -> sb.append(msg.getSender())
                            .append(": ")
                            .append(msg.getContent())
                            .append('\n'));
                    return sb.toString().trim();
                });
    }


    private static class ParseResult {
        String feedback = "Could not parse feedback from Gemini.";
        boolean pass = false;
    }

    private ParseResult parseFeedbackAndPassFromJson(String rawJson) {
        ParseResult result = new ParseResult();
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
            // 결과는 기본값 그대로
        }
        return result;
    }

    private String getEndMessage(String language) {
        return END_MESSAGE_MAP.getOrDefault(language.toLowerCase(), "The interview has ended.");
    }

    private static final Map<String, String> END_MESSAGE_MAP = Map.of(
            "english", "The interview has ended.",
            "korean", "면접이 종료되었습니다.",
            "japanese", "面接が終了しました。",
            "chinese", "面试结束了。",
            "spanish", "La entrevista ha terminado.",
            "french", "L'entretien est terminé.",
            "german", "Das Interview ist beendet.",
            "russian", "Собеседование завершено.",
            "portuguese", "A entrevista terminou.",
            "arabic", "تمت المقابلة."
    );

}
