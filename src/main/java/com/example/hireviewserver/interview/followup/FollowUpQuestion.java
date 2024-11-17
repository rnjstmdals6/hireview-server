package com.example.hireviewserver.interview.followup;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("follow_up_questions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpQuestion {
    @Id
    private Long id;
    private Long questionId;      // 원본 질문 ID
    private String content;       // 꼬리질문 내용
    private LocalDateTime createdAt;

    public FollowUpQuestion(Long questionId, String content) {
        this.questionId = questionId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}

