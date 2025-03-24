package com.example.hireviewserver.domain.interview.question;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("questions")
@Getter @NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    private Long id;
    private String content;
    private Integer priority;
    private Integer difficulty;
    private Long jobId;
    private String tags;
}