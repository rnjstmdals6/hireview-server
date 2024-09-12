package com.example.hireviewserver.feedback;

import com.example.hireviewserver.gemini.GeminiRequestDTO;
import com.example.hireviewserver.gemini.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final GeminiService geminiService;
    private final FeedbackRepository feedbackRepository;

    public Flux<String> getFeedbackByQuestion(FeedbackRequestDTO request) {
        String feedbackCommand = "전문 면접관처럼 질문에 대답을 잘했는지 상세하게 피드백 해줘." +
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

        return geminiService.generateContentStream(requestDTO)
                .flatMap(response -> {
                    // 피드백에서 면접 점수를 추출하는 로직
                    int score = extractScore(response);
                    if (score > 0) {
                        Feedback feedback = new Feedback(score, response, request.getQuestionId());
                        return feedbackRepository.save(feedback)
                                .thenReturn(response);
                    } else {
                        return Mono.just(response);
                    }
                });
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

