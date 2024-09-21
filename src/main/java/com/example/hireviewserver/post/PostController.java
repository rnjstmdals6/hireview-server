package com.example.hireviewserver.post;

import com.example.hireviewserver.common.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/api/v1/posts")
    public Mono<PageResponseDTO<PostResponseDTO>> getAllPostsByCategory(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.getAllPostsByCategory(category, page, size);
    }
}
