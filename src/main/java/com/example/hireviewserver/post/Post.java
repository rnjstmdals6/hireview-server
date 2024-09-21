package com.example.hireviewserver.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("posts")
@Getter
@NoArgsConstructor
public class Post {
    @Id
    private Long id;
    private String title;
    private String description;
    private String category;
    private Long userId;
    private LocalDateTime createdAt;

    public Post(String title, String description, String category, LocalDateTime createAt, Long userId) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.createdAt = createAt;
        this.userId = userId;
    }
}
