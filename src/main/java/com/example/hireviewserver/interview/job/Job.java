package com.example.hireviewserver.interview.job;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("jobs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    @Id
    private Long id;
    private Long categoryId;
    private String name;
}
