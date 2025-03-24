package store.hireview.domain.community.comment;

import store.hireview.domain.user.User;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class CommentResponseDTO {
    private Long id;
    private String description;
    private String email;
    private String name;
    private String picture;
    private String createdAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); // 년-월-일 시:분

    public CommentResponseDTO(Comment comment, User user) {
        this.id = comment.getId();
        this.description = comment.getDescription();
        this.email = user.getEmail();
        this.name = user.getName();
        this.picture = user.getPicture();
        this.createdAt = comment.getCreatedAt().format(FORMATTER);
    }
}
