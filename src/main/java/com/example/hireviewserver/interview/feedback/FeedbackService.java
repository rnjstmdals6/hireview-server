package com.example.hireviewserver.interview.feedback;

import com.example.hireviewserver.gemini.GeminiRequestDTO;
import com.example.hireviewserver.gemini.GeminiService;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final GeminiService geminiService;
    private final ApplicationEventPublisher eventPublisher;

    public Flux<String> getFeedbackByQuestion(FeedbackRequestDTO request) {
        String feedbackCommand = request.getJob() + " 전문 면접관처럼 질문에 대답을 잘했는지 상세하게 피드백 해줘." +
                " 더해서 어떻게 답변하면 좋을지 추천해주고 예상 정답도 알려줘. 그리고 반드시 마지막 문장에 객관적이고 냉철하게 판단해서 '면접 점수: 몇점/10' 형식으로 표현해줘";
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
                    int score = extractScore(finalResponse.toString());
                    eventPublisher.publishEvent(new FeedbackSaveEvent(this, finalResponse.toString(), score, request.getQuestionId()));
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

    private int extractScore(String feedback) {
        Pattern pattern = Pattern.compile("면접 점수: (\\d+)/10");
        Matcher matcher = pattern.matcher(feedback);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1)); // 점수 추출
        }
        return 1;
    }
}
