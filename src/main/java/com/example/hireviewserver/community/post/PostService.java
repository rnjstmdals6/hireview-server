package com.example.hireviewserver.community.post;

import com.example.hireviewserver.common.PageResponseDTO;
import com.example.hireviewserver.community.like.LikeService;
import com.example.hireviewserver.user.User;
import com.example.hireviewserver.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final LikeService likeService;

    private final Map<String, Instant> viewHistory = new ConcurrentHashMap<>();

    public Mono<PageResponseDTO<PostResponseDTO>> getAllPostByCategory(String category, int page, int size, Long userId) {
        Mono<Long> total = postRepository.countByCategory(category);

        Flux<PostResponseDTO> posts = postRepository.findAllByCategoryWithPagination(category, size, page * size)
                .flatMap(post -> {
                    Mono<Long> likes = likeService.getLikeCount(post.getId());
                    Mono<Boolean> liked = likeService.existsByPostIdAndUserId(post.getId(), userId);

                    return userService.findUserById(post.getUserId())
                            .flatMap(user -> Mono.zip(Mono.just(post), Mono.just(user), likes, liked))
                            .map(tuple -> {
                                Post postData = tuple.getT1();
                                User userData = tuple.getT2();
                                Long likeCount = tuple.getT3();
                                Boolean isLiked = tuple.getT4();

                                PostResponseDTO response = new PostResponseDTO(postData, userData, likeCount);
                                response.setIsLiked(isLiked);
                                return response;
                            });
                });

        return total.zipWith(posts.collectList(), (totalElements, postList) ->
                new PageResponseDTO<>(postList, totalElements, page));
    }


    public Mono<PostResponseDTO> createPost(PostRequestDTO dto, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(userService::findUserIdByEmail)
                .flatMap(userId -> postRepository.save(new Post(dto, userId)))
                .flatMap(post -> userService.findUserById(post.getUserId())
                        .map(user -> new PostResponseDTO(post, user, 0L)));

    }

    public Mono<PostResponseDTO> getPostById(Long postId, String ipAddress, Long userId) {
        String key = postId + ":" + ipAddress;
        Instant now = Instant.now();

        return postRepository.findById(postId)
                .flatMap(post -> {
                    if (!viewHistory.containsKey(key) || Duration.between(viewHistory.get(key), now).toMinutes() >= 180) {
                        post.increaseView();
                        viewHistory.put(key, now);
                        return postRepository.save(post);
                    }
                    return Mono.just(post);
                })
                .flatMap(post -> Mono.zip(
                        userService.findUserById(post.getUserId()),
                        likeService.getLikeCount(postId),
                        likeService.existsByPostIdAndUserId(postId, userId)
                ).map(tuple -> {
                    User user = tuple.getT1();
                    Long likes = tuple.getT2();
                    Boolean liked = tuple.getT3();

                    PostResponseDTO response = new PostResponseDTO(post, user, likes);
                    response.setIsLiked(liked);
                    return response;
                }));
    }

}
