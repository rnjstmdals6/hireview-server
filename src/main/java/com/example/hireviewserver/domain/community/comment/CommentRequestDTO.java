package com.example.hireviewserver.domain.community.comment;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommentRequestDTO {
    private Long postId;
    private String description;
}
