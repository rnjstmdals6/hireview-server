package com.example.hireviewserver.domain.interview.feedback;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackResponseDTO {
    private Long id;
    private Double score;
    private String question;
    private String answer;
    private String feedback;
    private String job;
    private Integer priority;
    private Integer difficulty;
}
