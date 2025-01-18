package com.example.hireviewserver.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserRankingResponseDTO {
    private String name;
    private String picture;
    private Integer score;
    private Integer ranking;
}