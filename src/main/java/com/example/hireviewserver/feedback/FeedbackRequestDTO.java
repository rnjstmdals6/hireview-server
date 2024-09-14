package com.example.hireviewserver.feedback;

import com.example.hireviewserver.common.enums.Job;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FeedbackRequestDTO {
    private Long questionId;
    private String question;
    private String answer;
    private String job;
}
