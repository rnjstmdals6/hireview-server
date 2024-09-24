package com.example.hireviewserver.user;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 유저를 이메일로 찾거나 없으면 생성
    public Mono<User> findOrCreateUser(String email, String name, String picture) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(userRepository.save(new User(email, name, picture)));
    }

    // 유저 ID로 찾기
    public Mono<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    // 유저 이메일로 찾기
    public Mono<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 유저 ID 가져오기
    public Mono<Long> findUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId);
    }
}