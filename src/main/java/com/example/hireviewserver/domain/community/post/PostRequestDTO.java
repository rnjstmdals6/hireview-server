package com.example.hireviewserver.domain.community.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
}
