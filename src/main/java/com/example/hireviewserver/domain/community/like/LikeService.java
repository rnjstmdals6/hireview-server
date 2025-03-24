package com.example.hireviewserver.domain.community.like;

import com.example.hireviewserver.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserService userService;

    public Mono<Void> addOrDeleteLike(Long postId, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(userService::findUserIdByEmail) // 이메일을 통해 유저 ID 조회
                .flatMap(userId -> existsByPostIdAndUserId(postId, userId)
                        .flatMap(exists -> {
                            if (!exists) {
                                Like like = new Like(postId, userId);
                                return likeRepository.save(like).then();
                            } else {
                                return likeRepository.deleteByPostIdAndUserId(postId, userId); // 이미 좋아요를 눌렀다면 삭제
                            }
                        }));
    }

    public Mono<Long> getLikeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    public Mono<Boolean> existsByPostIdAndUserId(Long postId, Long userId) {
        return likeRepository.countByPostIdAndUserId(postId, userId)
                .map(count -> count > 0);
    }

    public Mono<Boolean> existsByPostIdAndEmail(Long postId, String email) {
        return userService.findUserIdByEmail(email)
                .flatMap(userId -> likeRepository.countByPostIdAndUserId(postId, userId))
                .map(count -> count > 0);
    }
}
