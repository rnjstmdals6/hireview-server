package store.hireview.domain.community.like;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("likes")
@Getter
@NoArgsConstructor
public class Like {
    @Id
    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime likedAt = LocalDateTime.now();

    public Like(Long postId, Long userId) {
        this.postId = postId;
        this.userId = userId;
    }
}