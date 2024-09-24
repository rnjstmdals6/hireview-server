package com.example.hireviewserver.post;

import com.example.hireviewserver.common.PageResponseDTO;
import com.example.hireviewserver.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/api/v1/all-post")
    public Mono<PageResponseDTO<PostResponseDTO>> getAllPostByCategory(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.getAllPostByCategory(category, page, size);
    }

    @GetMapping("/api/v1/post")
    public Mono<PostResponseDTO> getPostById(@RequestParam Long postId
    ) {
        return postService.getPostById(postId);
    }

    @PostMapping("api/v1/post")
    public Mono<PostResponseDTO> createPost(Mono<Principal> principal, @RequestBody PostRequestDTO dto) {
        return postService.createPost(dto, principal);
    }
}
