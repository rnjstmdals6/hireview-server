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

    // 게시글 생성
    @PostMapping("/api/v1/post")
    public Mono<PostResponseDTO> createPost(Mono<Principal> principal, @RequestBody PostRequestDTO dto) {
        return postService.createPost(dto, principal);
    }

    @GetMapping("/api/v1/all-post")
    public Mono<PageResponseDTO<PostResponseDTO>> getAllPostByCategory(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Mono<Principal> principal
    ) {
        return principal
                .map(Principal::getName)
                .flatMap(userId -> postService.getAllPostByCategory(category, page, size, Long.valueOf(userId)))
                .switchIfEmpty(postService.getAllPostByCategory(category, page, size, null));
    }

    @GetMapping("/api/v1/post")
    public Mono<PostResponseDTO> getPostById(@RequestParam Long postId, ServerHttpRequest request, Mono<Principal> principal) {
        String ipAddress = Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress();

        return principal
                .map(Principal::getName)
                .flatMap(userId -> postService.getPostById(postId, ipAddress, Long.valueOf(userId)))
                .switchIfEmpty(postService.getPostById(postId, ipAddress, null));
    }
}
