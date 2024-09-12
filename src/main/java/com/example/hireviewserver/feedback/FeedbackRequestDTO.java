package com.example.hireviewserver.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FeedbackRequestDTO {
    private Long questionId;
    private String question;
    private String answer;
}
