package com.example.hireviewserver.community.like;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface LikeRepository extends ReactiveCrudRepository<Like, Long> {

    // Boolean 대신 Long을 반환하고, 이를 수동으로 변환하도록 변경
    @Query("SELECT COUNT(*) FROM likes WHERE post_id = :postId AND user_id = :userId")
    Mono<Long> countByPostIdAndUserId(Long postId, Long userId);

    @Query("SELECT COUNT(*) FROM likes WHERE post_id = :postId")
    Mono<Long> countByPostId(Long postId);

    Mono<Void> deleteByPostIdAndUserId(Long postId, Long userId);
}
