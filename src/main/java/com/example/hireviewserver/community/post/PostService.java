package com.example.hireviewserver.community.post;

import com.example.hireviewserver.common.PageResponseDTO;
import com.example.hireviewserver.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;

    public Mono<PageResponseDTO<PostResponseDTO>> getAllPostByCategory(String category, int page, int size) {
        Mono<Long> total = postRepository.countByCategory(category);

        Flux<PostResponseDTO> posts = postRepository.findAllByCategoryWithPagination(category, size, page * size)
                .flatMap(post -> userService.findUserById(post.getUserId())
                        .map(user -> new PostResponseDTO(post, user)));

        return total.zipWith(posts.collectList(), (totalElements, postList) ->
                new PageResponseDTO<>(postList, totalElements, page));
    }

    public Mono<PostResponseDTO> createPost(PostRequestDTO dto, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(userService::findUserIdByEmail)
                .flatMap(userId -> postRepository.save(new Post(dto, userId)))
                .flatMap(post -> userService.findUserById(post.getUserId())
                        .map(user -> new PostResponseDTO(post, user)));

    }

    public Mono<PostResponseDTO> getPostById(Long postId) {
        return postRepository.findById(postId)
                .flatMap(post -> userService.findUserById(post.getUserId())
                        .map(user -> new PostResponseDTO(post, user)));
    }
}
