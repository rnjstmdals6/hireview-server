package com.example.hireviewserver.interview.feedback;

import com.example.hireviewserver.common.PageResponseDTO;
import com.example.hireviewserver.gemini.GeminiRequestDTO;
import com.example.hireviewserver.gemini.GeminiService;
import com.example.hireviewserver.interview.question.Question;
import com.example.hireviewserver.interview.question.QuestionService;
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

import java.util.ArrayList;
import java.util.List;
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
        // 평가 기준을 명확히 하여 점수가 엄격하게 주어지도록 함
        String feedbackCommand = request.getJob() + " as a professional interviewer, provide a detailed, objective feedback on the answer based on the following criteria:" +
                " (1) Accuracy: How accurate and relevant is the answer to the question?" +
                " (2) Completeness: Does the answer cover all important aspects?" +
                " (3) Clarity: Is the answer well-organized and easy to understand?" +
                " (4) Originality: Does the answer demonstrate unique insight?" +
                " (5) Conciseness: Is the answer clear without unnecessary information?" +
                " Deduct points for each area where the answer is lacking." +
                " Suggest specific ways to improve each aspect and provide an ideal answer." +
                " Assign a final score out of 10 based on the overall performance with the following format 'Interview Score: X/10'. Be conservative in scoring, with 10 being excellent and 1 being very poor.";

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

        return geminiService.generateContentStream(requestDTO)
                .map(this::extractTextFromResponse) // JSON에서 텍스트만 추출
                .doOnComplete(() -> {
                    geminiService.generateContentStream(requestDTO)
                            .collectList()
                            .subscribe(fullResponses -> {
                                String completeResponse = String.join("", fullResponses);
                                Double score = extractScore(completeResponse);
                                eventPublisher.publishEvent(new FeedbackSaveEvent(
                                        this, completeResponse, score, request.getQuestionId(), email, request.getAnswer()
                                ));
                            });
                });
    }

    private Double extractScore(String feedback) {
        Pattern pattern = Pattern.compile("Interview Score: (\\d+)/10");
        Matcher matcher = pattern.matcher(feedback);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1)); // 점수 추출
        }
        return 1.0;
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
                            feedbackText.append(parts.getJSONObject(j).getString("text")); // 텍스트 필드를 추가
                        }
                    }
                }
            }

            return feedbackText.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return ""; // 오류 발생 시 빈 문자열 반환
        }
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
                                    responseDTO.setFeedback(feedback.getFeedback());
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
                    long personalityScoreSum = 0;
                    long behavioralScoreSum = 0;
                    int personalityCount = 0;
                    int behavioralCount = 0;

                    for (Tuple2<Feedback, Question> pair : feedbackQuestionPairs) {
                        Feedback feedback = pair.getT1();
                        Question question = pair.getT2();

                        if (question.getJobId() == 1L) {
                            personalityScoreSum += feedback.getScore();
                            personalityCount++;
                        } else {
                            behavioralScoreSum += feedback.getScore();
                            behavioralCount++;
                        }
                    }

                    long personalityScoreAvg = (personalityCount > 0) ? personalityScoreSum / personalityCount : 0;
                    long behavioralScoreAvg = (behavioralCount > 0) ? behavioralScoreSum / behavioralCount : 0;

                    FeedbackStatResponseDTO responseDTO = new FeedbackStatResponseDTO();
                    responseDTO.setPersonalityScore(personalityScoreAvg);
                    responseDTO.setBehavioralScore(behavioralScoreAvg);
                    responseDTO.setPersonalityCount(personalityCount);
                    responseDTO.setBehavioralCount(behavioralCount);

                    return Mono.just(responseDTO);
                });
    }
}
