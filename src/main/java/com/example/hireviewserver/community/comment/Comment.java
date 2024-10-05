package com.example.hireviewserver.community.comment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("comments")
@Getter
@NoArgsConstructor
public class Comment {

    @Id
    private Long id;
    private String description;
    private LocalDateTime createdAt;
    private Long userId;
    private Long postId;

    public Comment(CommentRequestDTO dto, Long userId) {
        this.description = dto.getDescription();
        this.postId = dto.getPostId();
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    public void modify(CommentRequestDTO dto) {
        this.description = dto.getDescription();
    }
}
