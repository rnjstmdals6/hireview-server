package com.example.hireviewserver.interview.feedback;

import com.example.hireviewserver.user.UserRankingResponseDTO;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Repository
public interface FeedbackRepository extends ReactiveCrudRepository<Feedback, Long> {
    @Query("SELECT * FROM feedbacks WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    Flux<Feedback> findAllByUserId(Long userId, int limit, int offset);

    @Query("SELECT * FROM feedbacks WHERE user_id = :userId")
    Flux<Feedback> findAllByUserId(Long userId);

    @Query("""
            SELECT u.name AS name, 
                   u.picture AS picture, 
                   SUM(f.score) AS score, 
                   RANK() OVER (ORDER BY SUM(f.score) DESC) AS ranking
            FROM users u
            LEFT JOIN feedbacks f ON u.id = f.user_id
            GROUP BY u.name, u.picture
            ORDER BY score DESC
            LIMIT 5;
            """)
    Flux<UserRankingResponseDTO> findTop5UsersByScore();

    @Query("""
            SELECT u.name AS name, 
                   u.picture AS picture, 
                   SUM(f.score) AS score,  
                   COALESCE(RANK() OVER (ORDER BY SUM(f.score) DESC), 0) AS ranking
            FROM users u
            LEFT JOIN feedbacks f ON u.id = f.user_id
            WHERE u.id = :userId
            GROUP BY u.name, u.picture
            """)
    Mono<UserRankingResponseDTO> findUserRankingByUserId(Long userId);

    @Query("""
        SELECT 
            AVG(accuracy) AS accuracy, 
            AVG(completeness) AS completeness, 
            AVG(logicality) AS logicality
        FROM feedbacks
        WHERE user_id = :userId
    """)
    Mono<FeedbackAbilityDTO> findAverageStatsByUserId(@Param("userId") Long userId);
}
