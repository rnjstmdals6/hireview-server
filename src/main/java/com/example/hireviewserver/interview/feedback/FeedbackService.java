package com.example.hireviewserver.interview.feedback;

import com.example.hireviewserver.common.PageResponseDTO;
import com.example.hireviewserver.gemini.GeminiRequestDTO;
import com.example.hireviewserver.gemini.GeminiService;
import com.example.hireviewserver.interview.question.Question;
import com.example.hireviewserver.interview.question.QuestionService;
import com.example.hireviewserver.user.UserRankingResponseDTO;
import com.example.hireviewserver.user.UserService;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final GeminiService geminiService;
    private final ApplicationEventPublisher eventPublisher;
    private final FeedbackRepository feedbackRepository;
    private final UserService userService;
    private final QuestionService questionService;

    public Flux<String> getFeedbackByQuestion(FeedbackRequestDTO request, String email) {

        String feedbackCommand = request.getJob() + " as a professional interviewer, provide a detailed, objective feedback on the answer based on the following criteria:" +
                                 " Accuracy: How accurate and relevant is the answer to the question? Provide a score in the format 'Accuracy: X/10'." +
                                 " Completeness: Does the answer cover all important aspects? Provide a score in the format 'Completeness: X/10'." +
                                 " Logicality: Is the answer logically sound, well-reasoned, and coherent? Provide a score in the format 'Logicality: X/10'." +
                                 " Deduct points for each area where the answer is lacking." +
                                 " Suggest specific ways to improve each aspect and provide an ideal answer." +
                                 " Assign a final overall score out of 10 based on the overall performance with the following format 'Interview Score: X/10'. Be conservative in scoring, with 10 being excellent and 1 being very poor.";

        String questionAndAnswer = "Question : " + request.getQuestion() + " Answer : " + request.getAnswer();

        GeminiRequestDTO.Content feedbackContent = GeminiRequestDTO.Content.builder()
                .role("user")
                .parts(List.of(GeminiRequestDTO.Content.ContentPart.builder()
                        .text(feedbackCommand)
                        .build()))
                .build();

        GeminiRequestDTO.Content questionAnswerContent = GeminiRequestDTO.Content.builder()
                .role("user")
                .parts(List.of(GeminiRequestDTO.Content.ContentPart.builder()
                        .text(questionAndAnswer)
                        .build()))
                .build();

        GeminiRequestDTO requestDTO = GeminiRequestDTO.builder()
                .contents(List.of(feedbackContent, questionAnswerContent))
                .build();

        StringBuilder finalResponse = new StringBuilder();
        return geminiService.generateContentStream(requestDTO)
                .doOnNext(response -> {
                    String parsedText = extractTextFromResponse(response);
                    finalResponse.append(parsedText);
                })
                .doOnComplete(() -> {
                    Map<String, Double> scores = extractScores(finalResponse.toString());
                    Double totalScore = scores.getOrDefault("Interview Score", 1.0);
                    Double accuracyScore = scores.getOrDefault("Accuracy", 1.0);
                    Double completenessScore = scores.getOrDefault("Completeness", 1.0);
                    Double logicalityScore = scores.getOrDefault("Logicality", 1.0);

                    eventPublisher.publishEvent(new FeedbackSaveEvent(this, finalResponse.toString(), totalScore, request.getQuestionId(), email, request.getAnswer(), accuracyScore, completenessScore, logicalityScore));
                });
    }

    private String extractTextFromResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            StringBuilder feedbackText = new StringBuilder();

            for (int i = 0; i < candidates.length(); i++) {
                JSONObject candidate = candidates.getJSONObject(i);

                if (candidate.has("content")) {
                    JSONObject content = candidate.getJSONObject("content");

                    if (content.has("parts")) {
                        JSONArray parts = content.getJSONArray("parts");

                        for (int j = 0; j < parts.length(); j++) {
                            feedbackText.append(parts.getJSONObject(j).getString("text")); // text 필드 추출
                        }
                    }
                }
            }

            return feedbackText.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return ""; // 파싱 오류 시 빈 문자열 반환
        }
    }

    private Map<String, Double> extractScores(String feedback) {
        Map<String, Double> scores = new HashMap<>();
        Pattern pattern = Pattern.compile("(Accuracy|Completeness|Logicality|Interview Score): (\\d+)/10");
        Matcher matcher = pattern.matcher(feedback);

        while (matcher.find()) {
            String category = matcher.group(1); // Accuracy, Completeness, Logicality, Interview Score
            Double score = Double.parseDouble(matcher.group(2)); // 점수 추출
            scores.put(category, score);
        }

        return scores;
    }

    public Mono<PageResponseDTO<FeedbackResponseDTO>> getAllFeedback(int page, int size, String email) {
        return userService.findUserIdByEmail(email)
                .flatMap(userId -> feedbackRepository.findAllByUserId(userId, size, page * size)
                        .flatMap(feedback -> questionService.findQuestionById(feedback.getQuestionId()) // 질문 정보 조회
                                .map(question -> {
                                    FeedbackResponseDTO responseDTO = new FeedbackResponseDTO();
                                    responseDTO.setId(feedback.getId());
                                    responseDTO.setScore(feedback.getScore());
                                    responseDTO.setQuestion(question.getContent());
                                    responseDTO.setFeedback(feedback.getContent());
                                    responseDTO.setAnswer(feedback.getAnswer());
                                    responseDTO.setPriority(question.getPriority());
                                    responseDTO.setDifficulty(question.getDifficulty());
                                    return responseDTO;
                                })
                        )
                        .collectList()
                        .flatMap(feedbackList -> Mono.just(new PageResponseDTO<>(feedbackList, page, size)))
                );
    }

    public Mono<FeedbackStatResponseDTO> getFeedbackStat(String email) {
        return userService.findUserIdByEmail(email)
                .flatMapMany(feedbackRepository::findAllByUserId)
                .flatMap(feedback -> questionService.findQuestionById(feedback.getQuestionId())
                        .map(question -> Tuples.of(feedback, question))
                )
                .collectList()
                .flatMap(feedbackQuestionPairs -> {
                    long behavioralScoreSum = 0;
                    int behavioralCount = 0;

                    for (Tuple2<Feedback, Question> pair : feedbackQuestionPairs) {
                        Feedback feedback = pair.getT1();

                        
                        behavioralScoreSum += feedback.getScore();
                        behavioralCount++;
                    }

                    long behavioralScoreAvg = (behavioralCount > 0) ? behavioralScoreSum / behavioralCount : 0;

                    FeedbackStatResponseDTO responseDTO = new FeedbackStatResponseDTO();
                    responseDTO.setBehavioralScore(behavioralScoreAvg);
                    responseDTO.setBehavioralCount(behavioralCount);

                    return Mono.just(responseDTO);
                });
    }

    public Flux<UserRankingResponseDTO> getTop5Rankings() {
        return feedbackRepository.findTop5UsersByScore();
    }

    public Mono<UserRankingResponseDTO> getUserRanking(String name) {
        return userService.findUserIdByName(name)
                        .flatMap(feedbackRepository::findUserRankingByUserId);
    }

    public Mono<FeedbackAbilityDTO> getUserAverageStats(String email) {
        return userService.findUserIdByEmail(email)
                .flatMap(feedbackRepository::findAverageStatsByUserId);
    }
}
