package com.example.hireviewserver.interview.followup;

import com.example.hireviewserver.gemini.GeminiRequestDTO;
import com.example.hireviewserver.gemini.GeminiService;
import com.example.hireviewserver.interview.question.QuestionRepository;
import com.example.hireviewserver.user.UserRepository;
import com.example.hireviewserver.user.UserService;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowUpQuestionService {
    private final UserService userService;
    private final FollowUpQuestionRepository followUpQuestionRepository;
    private final QuestionRepository questionRepository;
    private final GeminiService geminiService;

    public Flux<String> generateFollowUpQuestion(FollowUpQuestionRequestDTO dto, String email) {
        return questionRepository.findById(dto.getQuestionId())
                .switchIfEmpty(Mono.error(new RuntimeException("Question not found")))
                .flatMapMany(question -> {
                    String prompt = String.format(
                            "Here is the original question asked to the user: \"%s\". " +
                                    "The user's answer to this question is as follows: \"%s\". " +
                                    "Based on this information, create one highly relevant and thought-provoking follow-up question. " +
                                    "Ensure the follow-up question is concise, specific, and directly tied to the original question and the user's response. " +
                                    "The follow-up question should not exceed 50 words.",
                            question.getContent(), dto.getAnswer()
                    );

                    GeminiRequestDTO requestDTO = GeminiRequestDTO.builder()
                            .contents(List.of(
                                    GeminiRequestDTO.Content.builder()
                                            .role("user")
                                            .parts(List.of(GeminiRequestDTO.Content.ContentPart.builder()
                                                    .text(prompt)
                                                    .build()))
                                            .build()
                            ))
                            .build();

                    return userService.findUserIdByEmail(email)
                            .flatMapMany(userId -> geminiService.generateContentStream(requestDTO)
                                    .map(this::extractTextFromResponse)
                                    .collectList()
                                    .flatMapMany(contents -> {
                                        FollowUpQuestion followUpQuestion = new FollowUpQuestion(
                                                dto.getQuestionId(),
                                                userId,
                                                String.join(" ", contents)
                                        );
                                        return followUpQuestionRepository.save(followUpQuestion)
                                                .thenMany(Flux.fromIterable(contents));
                                    })
                            );
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
                            feedbackText.append(parts.getJSONObject(j).getString("text"));
                        }
                    }
                }
            }

            return feedbackText.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    // 특정 질문 ID로 연결된 모든 꼬리질문 조회
    public Flux<FollowUpQuestion> getFollowUpQuestionsByQuestionId(Long questionId) {
        return followUpQuestionRepository.findAllByQuestionId(questionId);
    }
}