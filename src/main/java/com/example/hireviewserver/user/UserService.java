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

    public Mono<Void> setUserName(String email, SetUserNameRequestDTO dto) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    user.setName(dto.getName());
                    return userRepository.save(user);
                })
                .then();
    }

    public Mono<Void> setUserJob(String email, SetUserJobRequestDTO dto) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    user.setJob(dto.getJob());
                    return userRepository.save(user);
                })
                .then();
    }

    public Mono<Void> deleteUser(String email) {
        return userRepository.findByEmail(email)
                .flatMap(userRepository::delete)
                .then();
    }

    public Mono<User> consumeToken(String email) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    if (user.getToken() == null || user.getToken() <= 0) {
                        return Mono.error(new IllegalStateException("Not enough tokens"));
                    }
                    user.decreaseToken();
                    return userRepository.save(user);
                });
    }
}