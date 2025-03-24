package com.example.hireviewserver.domain.community.post;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

@Repository
public interface PostRepository extends ReactiveCrudRepository<Post, Long> {
    @Query("SELECT COUNT(*) FROM posts WHERE (:category IS NULL OR category = :category)")
    Mono<Long> countByCategory(@Nullable String category);
    @Query("SELECT * FROM posts WHERE (:category IS NULL OR category = :category) ORDER BY created_at DESC, id DESC LIMIT :limit OFFSET :offset")
    Flux<Post> findAllByCategoryWithPagination(@Nullable String category, int limit, int offset);
    // 특정 사용자의 게시글 개수
    Mono<Long> countAllByUserId(Long userId);
    // 특정 사용자의 게시글 조회 (페이지네이션 포함)
    @Query("SELECT * FROM posts WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<Post> findAllByUserId(Long userId, int limit, int offset);
    @Query("DELETE FROM posts WHERE id = :postId AND user_id = :userId")
    Mono<Void> deleteByIdAndUserId(Long postId, Long userId);
}
