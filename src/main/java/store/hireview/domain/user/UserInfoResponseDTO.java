package store.hireview.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserInfoResponseDTO {
    private String email;
    private String job;
    private String name;
    private String picture;
    private Integer token;

    public UserInfoResponseDTO(User user) {
        this.email = user.getEmail();
        this.job = user.getJob();
        this.name = user.getName();
        this.picture = user.getPicture();
        this.token = user.getToken();
    }
}
