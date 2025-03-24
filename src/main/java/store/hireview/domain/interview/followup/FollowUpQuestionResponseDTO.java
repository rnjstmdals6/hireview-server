package store.hireview.domain.interview.followup;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FollowUpQuestionResponseDTO {
    private Long questionId;  // 원본 질문 ID
    private String content;   // 생성된 꼬리질문
}
