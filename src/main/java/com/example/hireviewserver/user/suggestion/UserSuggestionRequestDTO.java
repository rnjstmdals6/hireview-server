package com.example.hireviewserver.user.suggestion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSuggestionRequestDTO {
    private String content;
    private String evaluation;
}
