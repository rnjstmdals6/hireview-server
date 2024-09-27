package com.example.hireviewserver.community.like;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface LikeRepository extends ReactiveCrudRepository<Like, Long> {

    @Query("SELECT COUNT(*) > 0 FROM likes WHERE post_id = :postId AND user_id = :userId")
    Mono<Boolean> existsByPostIdAndUserId(Long postId, Long userId);

    @Query("SELECT COUNT(*) FROM likes WHERE post_id = :postId")
    Mono<Long> countByPostId(Long postId);

    Mono<Void> deleteByPostIdAndUserId(Long postId, Long userId);
}
