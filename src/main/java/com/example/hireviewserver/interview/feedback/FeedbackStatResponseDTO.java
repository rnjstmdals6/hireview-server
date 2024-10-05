package com.example.hireviewserver.interview.feedback;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackStatResponseDTO {
    private Long personalityScore;
    private Long behavioralScore;
    private Integer personalityCount;
    private Integer behavioralCount;
}
