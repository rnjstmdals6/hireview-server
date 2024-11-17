package com.example.hireviewserver.interview.followup;

import lombok.Data;

@Data
public class FollowUpQuestionRequestDTO {
    private Long questionId; // 질문 ID
    private String answer;   // 사용자의 답변
}
