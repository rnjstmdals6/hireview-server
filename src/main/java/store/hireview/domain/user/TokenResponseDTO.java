package store.hireview.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class TokenResponseDTO {
    private String accessToken;
    private String refreshToken;
}