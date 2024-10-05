package com.example.hireviewserver.community.post;

import com.example.hireviewserver.common.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/api/v1/post")
    public Mono<PostResponseDTO> createPost(Mono<Principal> principal, @RequestBody PostRequestDTO dto) {
        return postService.createPost(dto, principal);
    }

    @PutMapping("/api/v1/post")
    public Mono<PostResponseDTO> modifyPost(Mono<Principal> principal, @RequestBody PostRequestDTO dto) {
        return postService.modifyPost(dto, principal);
    }

    @DeleteMapping("/api/v1/post/{postId}")
    public Mono<Void> deletePost(@PathVariable Long postId, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(email -> postService.deletePost(postId, email));
    }

    @GetMapping("/api/v1/all-post")
    public Mono<PageResponseDTO<PostResponseDTO>> getAllPostByCategory(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "false") boolean myPosts,
            Mono<Principal> principal
    ) {
        return principal
                .map(Principal::getName)
                .flatMap(email -> {
                    if (myPosts) {
                        return postService.getMyPosts(page, size, email);
                    }
                    return postService.getAllPostByCategory(category, page, size, email);
                })
                .switchIfEmpty(postService.getAllPostByCategory(category, page, size, null));
    }

    @GetMapping("/api/v1/post")
    public Mono<PostResponseDTO> getPostById(@RequestParam Long postId, ServerHttpRequest request, Mono<Principal> principal) {
        String ipAddress = Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress();

        return principal
                .map(Principal::getName)
                .flatMap(email -> postService.getPostById(postId, ipAddress, email))
                .switchIfEmpty(postService.getPostById(postId, ipAddress, null));
    }
}
