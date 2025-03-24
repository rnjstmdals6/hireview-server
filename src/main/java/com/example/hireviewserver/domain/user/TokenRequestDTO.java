package com.example.hireviewserver.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TokenRequestDTO {
    private String refreshToken;
}
