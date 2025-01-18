package com.example.hireviewserver.interview.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackAbilityDTO {
    private Double accuracy;
    private Double completeness;
    private Double logicality;
}
