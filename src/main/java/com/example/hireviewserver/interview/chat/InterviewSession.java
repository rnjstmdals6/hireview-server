package com.example.hireviewserver.interview.chat;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("interview_sessions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSession {
    @Id
    private Long id;
    private String sessionId;
    private Long userId;
    private int currentStep;
    private LocalDateTime createdAt;

    public InterviewSession(Long userId) {
        this.sessionId = UUID.randomUUID().toString();
        this.userId = userId;
        this.currentStep = 0;
        this.createdAt = LocalDateTime.now();
    }

    public void incrementStep() {
        this.currentStep++;
    }
}
