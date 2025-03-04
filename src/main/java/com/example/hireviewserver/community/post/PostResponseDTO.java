package com.example.hireviewserver.community.post;

import com.example.hireviewserver.user.User;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
@Getter
@Setter
public class PostResponseDTO {
    private Long id;
    private Long views;
    private Long likes;
    private Long comments;
    private Boolean isLiked;
    private String category;
    private String title;
    private String description;
    private String email;
    private String name;
    private String picture;
    private String createdAt;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PostResponseDTO(Post post, User user, Long likes, Long comments) {
        this.id = post.getId();
        this.category = post.getCategory();
        this.title = post.getTitle();
        this.description = post.getDescription();
        this.email = user.getEmail();
        this.name = user.getName();
        this.picture = user.getPicture();
        this.createdAt = post.getCreatedAt().format(FORMATTER); // LocalDateTime을 포맷팅
        this.views = post.getViews();
        this.likes = likes;
        this.comments = comments;
    }

    public PostResponseDTO(Post post, User user, Long likes, Long comments, String description) {
        this.id = post.getId();
        this.category = post.getCategory();
        this.title = post.getTitle();
        this.description = description;
        this.email = user.getEmail();
        this.name = user.getName();
        this.picture = user.getPicture();
        this.createdAt = post.getCreatedAt().format(FORMATTER);
        this.views = post.getViews();
        this.likes = likes;
        this.comments = comments;
    }
}