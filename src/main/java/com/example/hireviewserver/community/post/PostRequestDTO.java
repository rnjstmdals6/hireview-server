package com.example.hireviewserver.community.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class PostRequestDTO {
    private String title;
    private String description;
    private String category;
}
