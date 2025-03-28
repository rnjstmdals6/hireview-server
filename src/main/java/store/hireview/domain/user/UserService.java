package store.hireview.domain.user;

import store.hireview.domain.user.attendance.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    @Value("${profile.image.upload-dir}")
    private String uploadDir;
    @Value("${app.base-url}")
    private String baseUrl;


    private final UserRepository userRepository;
    private final AttendanceService attendanceService;
    public Mono<User> findOrCreateUser(String email, String name, String picture) {
        return userRepository.findByEmail(email)
                .doOnNext(User::checkAttendance)
                .switchIfEmpty(
                        userRepository.save(new User(email, name, picture))
                                .doOnNext(User::checkAttendance)
                )
                .flatMap(user -> attendanceService.markAttendance(user.getId())
                        .thenReturn(user)
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

    public Mono<Long> findUserIdByName(String name) {
        return userRepository.findByName(name)
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
        String webpFileName = UUID.randomUUID() + ".webp";
        Path webpFilePath = Paths.get(uploadDir, webpFileName);

        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    String oldPictureUrl = user.getPicture();
                    if (oldPictureUrl != null && !oldPictureUrl.isEmpty()) {
                        Path oldFilePath = Paths.get(uploadDir, oldPictureUrl.replace(baseUrl + "/uploads/", ""));
                        return Mono.fromRunnable(() -> {
                                    try {
                                        Files.deleteIfExists(oldFilePath);
                                        log.info("Deleted old profile picture: " + oldFilePath);
                                    } catch (IOException e) {
                                        log.error("Failed to delete old profile picture: " + e.getMessage());
                                    }
                                }).subscribeOn(Schedulers.boundedElastic())
                                .thenReturn(user);
                    }
                    return Mono.just(user);
                })
                .flatMap(user -> filePart.transferTo(webpFilePath)
                        .then(Mono.fromRunnable(() -> {
                            try {
                                log.info("Converting image to WEBP using ProcessBuilder: " + webpFilePath);
                                ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/cwebp", webpFilePath.toString(), "-o", webpFilePath.toString());
                                processBuilder.redirectErrorStream(true);

                                Process process = processBuilder.start();
                                int exitCode = process.waitFor();
                                if (exitCode != 0) {
                                    throw new RuntimeException("Failed to convert image to WEBP format, exit code: " + exitCode);
                                }
                                log.info("Image converted to WEBP using ProcessBuilder: " + webpFilePath);
                            } catch (IOException | InterruptedException e) {
                                throw new RuntimeException("Failed to convert image to WEBP format", e);
                            }
                        }).subscribeOn(Schedulers.boundedElastic()))
                        .then(Mono.defer(() -> {
                            String fileUrl = baseUrl + "/uploads/" + webpFileName;
                            user.setPicture(fileUrl);
                            log.info("Profile picture saved to: " + fileUrl);
                            return userRepository.save(user);
                        })))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}