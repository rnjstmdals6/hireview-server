package com.example.hireviewserver.domain.user.suggestion;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSuggestionRepository extends ReactiveCrudRepository<UserSuggestion, Long> {
}
