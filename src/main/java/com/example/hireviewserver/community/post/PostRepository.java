package com.example.hireviewserver.community.post;

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

    @Query("SELECT * FROM posts WHERE (:category IS NULL OR category = :category) LIMIT :limit OFFSET :offset")
    Flux<Post> findAllByCategoryWithPagination(@Nullable String category, int limit, int offset);
}
