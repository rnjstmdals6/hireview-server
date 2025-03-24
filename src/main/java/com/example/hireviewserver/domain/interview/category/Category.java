package com.example.hireviewserver.domain.interview.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("categories")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    private Long id;
    private String name;
}
