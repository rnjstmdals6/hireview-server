package com.example.hireviewserver.post;

import com.example.hireviewserver.user.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Getter
@Setter
public class PostResponseDTO {
    private String title;
    private String description;
    private String email;
    private String name;
    private String picture;
    private String createdAt; // LocalDateTime 대신 String으로 반환

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // 년-월-일 시:분

    public PostResponseDTO(Post post, User user) {
        this.title = post.getTitle();
        this.description = post.getDescription();
        this.email = user.getEmail();
        this.name = user.getName();
        this.picture = user.getPicture();
        this.createdAt = post.getCreatedAt().format(FORMATTER); // LocalDateTime을 포맷팅
    }
}