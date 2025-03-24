package store.hireview.domain.community.post;

import store.hireview.common.response.PageResponse;
import store.hireview.domain.community.comment.CommentService;
import store.hireview.domain.community.like.LikeService;
import store.hireview.domain.user.User;
import store.hireview.domain.user.UserService;
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
    private final CommentService commentService;

    private final Map<String, Instant> viewHistory = new ConcurrentHashMap<>();

    public Mono<PageResponse<PostResponseDTO>> getAllPostByCategory(String category, int page, int size, String email) {
        Mono<Long> total = postRepository.countByCategory(category);

        Flux<PostResponseDTO> posts = postRepository.findAllByCategoryWithPagination(category, size, page * size)
                .concatMap(post -> {
                    if (post.getUserId() == null) {
                        return Mono.empty();
                    }

                    Mono<Long> likes = likeService.getLikeCount(post.getId());
                    Mono<Long> comments = commentService.getCommentCount(post.getId());
                    return userService.findUserById(post.getUserId())
                            .flatMap(user -> {
                                if (user == null) {
                                    return Mono.empty();
                                }
                                return Mono.zip(Mono.just(post), Mono.just(user), likes, comments)
                                        .map(tuple -> {
                                            Post postData = tuple.getT1();
                                            User userData = tuple.getT2();
                                            Long likeCount = tuple.getT3();
                                            Long commentCount = tuple.getT4();

                                            String content = postData.getDescription();
                                            if (content != null && content.length() > 80) {
                                                content = content.substring(0, 80) + "...";
                                            }

                                            return new PostResponseDTO(postData, userData, likeCount, commentCount, content);
                                        });
                            });
                });

        return total.zipWith(posts.collectList(), (totalElements, postList) ->
                new PageResponse<>(postList, totalElements, page));
    }

    public Mono<PostResponseDTO> createPost(PostRequestDTO dto, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(userService::findUserIdByEmail)
                .flatMap(userId -> postRepository.save(new Post(dto, userId)))
                .flatMap(post -> userService.findUserById(post.getUserId())
                        .map(user -> new PostResponseDTO(post, user, 0L, 0L)));

    }

    public Mono<PostResponseDTO> modifyPost(PostRequestDTO dto, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(userService::findUserIdByEmail)
                .flatMap(userId -> postRepository.findById(dto.getId())
                        .flatMap(post -> {
                            if (!post.getUserId().equals(userId)) {
                                return Mono.error(new RuntimeException("권한이 없습니다."));
                            }
                            post.modify(dto);
                            return postRepository.save(post)
                                    .flatMap(updatedPost -> userService.findUserById(updatedPost.getUserId())
                                            .map(user -> new PostResponseDTO(updatedPost, user, 0L, 0L)));
                        })
                );
    }

    public Mono<PostResponseDTO> getPostById(Long postId, String ipAddress, String email) {
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
                .flatMap(post -> {
                    Mono<Boolean> likedMono = Mono.just(false);

                    if (email != null) {
                        likedMono = likeService.existsByPostIdAndEmail(postId, email);
                    }

                    return Mono.zip(
                            userService.findUserById(post.getUserId()),
                            likeService.getLikeCount(postId),
                            likedMono,
                            commentService.getCommentCount(postId)
                    ).map(tuple -> {
                        User user = tuple.getT1();
                        Long likes = tuple.getT2();
                        Boolean liked = tuple.getT3();
                        Long comments = tuple.getT4();


                        PostResponseDTO response = new PostResponseDTO(post, user, likes, comments);
                        response.setIsLiked(liked);
                        return response;
                    });
                });
    }


    public Mono<PageResponse<PostResponseDTO>> getMyPosts(int page, int size, String email) {
        return userService.findUserIdByEmail(email)
                .flatMap(userId -> {
                    Mono<Long> total = postRepository.countAllByUserId(userId);  // 사용자 ID로 총 게시글 수 카운트

                    Flux<PostResponseDTO> posts = postRepository.findAllByUserId(userId, size, page * size)  // 사용자 ID로 게시글 찾기
                            .flatMap(post -> {
                                Mono<Long> likes = likeService.getLikeCount(post.getId());
                                Mono<Long> comments = commentService.getCommentCount(post.getId());

                                return userService.findUserById(post.getUserId())
                                        .flatMap(user -> Mono.zip(Mono.just(post), Mono.just(user), likes, comments))
                                        .map(tuple -> {
                                            Post postData = tuple.getT1();
                                            User userData = tuple.getT2();
                                            Long likeCount = tuple.getT3();
                                            Long commentCount = tuple.getT4();

                                            return new PostResponseDTO(postData, userData, likeCount, commentCount);
                                        });
                            });

                    return total.zipWith(posts.collectList(), (totalElements, postList) ->
                            new PageResponse<>(postList, totalElements, page));
                });
    }

    public Mono<Void> deletePost(Long postId, String email) {
        return userService.findUserIdByEmail(email).flatMap(
                userId -> postRepository.deleteByIdAndUserId(postId, userId)
        );
    }
}