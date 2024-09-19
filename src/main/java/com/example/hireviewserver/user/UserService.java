package com.example.hireviewserver.user;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<User> findOrCreateUser(String email, String name, String picture) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(
                        userRepository.save(new User(email, name, picture))
                );
    }

    public Mono<UserInfoResponseDTO> findUser(String email) {
        return userRepository.findByEmail(email)
                .map(UserInfoResponseDTO::new);
    }
}