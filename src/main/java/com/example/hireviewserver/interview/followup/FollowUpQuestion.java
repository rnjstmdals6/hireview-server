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
    private Long questionId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;

    public FollowUpQuestion(Long questionId, Long userId, String content) {
        this.questionId = questionId;
        this.userId = userId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}

