package com.example.hireviewserver.user.suggestion;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_suggestion")
@Getter
@AllArgsConstructor
public class UserSuggestion {
    @Id
    private Long id;
    private String content;
    private String evaluation;
    private Long userId;

    public UserSuggestion(Long userId, UserSuggestionRequestDTO dto) {
        this.userId = userId;
        this.content = dto.getContent();
        this.evaluation = dto.getEvaluation();
    }
}
