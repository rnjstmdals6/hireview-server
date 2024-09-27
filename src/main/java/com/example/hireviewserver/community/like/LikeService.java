package com.example.hireviewserver.community.like;

import com.example.hireviewserver.user.UserService;
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
                .flatMap(userId -> likeRepository.existsByPostIdAndUserId(postId, userId)
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
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }
}
