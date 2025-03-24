package com.example.hireviewserver.domain.interview.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("session_questions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SessionQuestion {
    @Id
    private Long id;
    private String sessionId;
    private Long questionId;
    private int step;
}