package com.example.hireviewserver.community.post;

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
    private Long views = 0L;
    private Long userId;
    private LocalDateTime createdAt;

    public Post(PostRequestDTO dto, Long userId) {
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.category = dto.getCategory();
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    public void modify(PostRequestDTO dto) {
        this.title = dto.getTitle();
        this.category = dto.getCategory();
        this.description = dto.getDescription();
    }

    public void increaseView() {
        this.views++;
    }
}
