package store.hireview.domain.community.comment;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CommentRepository extends ReactiveCrudRepository<Comment, Long> {
    @Query("SELECT COUNT(*) FROM comments WHERE post_id = :postId")
    Mono<Long> countByPostId(Long postId);

    @Query("SELECT * FROM comments WHERE post_id = :postId ORDER BY created_at ASC LIMIT :limit OFFSET :offset")
    Flux<Comment> findAllByPostIdWithPagination(Long postId, int limit, int offset);

    @Query("DELETE FROM comments WHERE id = :commentId AND user_id = :userId")
    Mono<Void> deleteByIdAndUserId(Long commentId, Long userId);
}
