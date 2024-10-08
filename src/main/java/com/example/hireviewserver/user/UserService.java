package com.example.hireviewserver.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    @Value("${profile.image.upload-dir}")
    private String uploadDir;
    @Value("${app.base-url}")
    private String baseUrl;


    private final UserRepository userRepository;
    // 유저를 이메일로 찾거나 없으면 생성
    public Mono<User> findOrCreateUser(String email, String name, String picture) {
        return userRepository.findByEmail(email)
                .doOnNext(User::checkAttendance)
                .switchIfEmpty(
                        userRepository.save(new User(email, name, picture))
                                .doOnNext(User::checkAttendance)
                )
                .flatMap(userRepository::save);
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

    public Mono<Void> saveUserProfilePicture(String email, FilePart filePart) {
        String fileName = UUID.randomUUID() + "_" + filePart.filename();
        Path filePath = Paths.get(uploadDir, fileName);

        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    String oldPictureUrl = user.getPicture();
                    if (oldPictureUrl != null && !oldPictureUrl.isEmpty()) {
                        Path oldFilePath = Paths.get(uploadDir, oldPictureUrl.replace(baseUrl + "/uploads/", ""));
                        return Mono.fromRunnable(() -> {
                                    try {
                                        Files.deleteIfExists(oldFilePath);
                                    } catch (IOException e) {
                                        System.err.println("Failed to delete old profile picture: " + e.getMessage());
                                    }
                                })
                                .subscribeOn(Schedulers.boundedElastic()) // 블로킹 작업을 별도의 스레드에서 실행
                                .thenReturn(user);
                    }
                    return Mono.just(user);
                })
                .flatMap(user -> Mono.fromRunnable(() -> {
                            try {
                                Files.createDirectories(filePath.getParent());
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to create directory for profile picture", e);
                            }
                        })
                        .subscribeOn(Schedulers.boundedElastic()) // 디렉터리 생성도 별도의 스레드에서 실행
                        .then(Mono.defer(() -> filePart.transferTo(filePath)))
                        .then(Mono.defer(() -> {
                            String fileUrl = baseUrl + "/uploads/" + fileName;
                            user.setPicture(fileUrl);
                            return userRepository.save(user);
                        })))
                .subscribeOn(Schedulers.boundedElastic()) // 파일 저장과 DB 업데이트도 별도의 스레드에서 실행
                .then();
    }
}