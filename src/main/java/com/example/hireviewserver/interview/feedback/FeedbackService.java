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
        String feedbackCommand = request.getJob() + " as a professional interviewer, provide detailed feedback on how well the answer addresses the question." +
                " Additionally, suggest ways to improve the answer, provide an ideal response, and make sure to include an objective score in the last sentence in the format 'Interview Score: X/10'.";
        String questionAndAnswer = "질문 : " + request.getQuestion() + " 답변 : " + request.getAnswer();

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
                    Double score = extractScore(finalResponse.toString());
                    eventPublisher.publishEvent(new FeedbackSaveEvent(this, finalResponse.toString(), score, request.getQuestionId(), email, request.getAnswer()));
                });
    }

    private String extractTextFromResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            StringBuilder feedbackText = new StringBuilder();

            for (int i = 0; i < candidates.length(); i++) {
                JSONObject candidate = candidates.getJSONObject(i);

                // content 필드가 존재하는지 확인
                if (candidate.has("content")) {
                    JSONObject content = candidate.getJSONObject("content");

                    // parts 필드가 존재하는지 확인
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

    private Double extractScore(String feedback) {
        Pattern pattern = Pattern.compile("Interview Score: (\\d+)/10");
        Matcher matcher = pattern.matcher(feedback);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1)); // 점수 추출
        }
        return 1.0;
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
