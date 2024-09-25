package com.example.hireviewserver.community.comment;

import com.example.hireviewserver.common.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/v1/comment")
    public Mono<CommentResponseDTO> createPost(Mono<Principal> principal, @RequestBody CommentRequestDTO dto) {
        return commentService.createComment(dto, principal);
    }

    @GetMapping("/api/v1/all-comment")
    public Mono<PageResponseDTO<CommentResponseDTO>> getAllComment(
            @RequestParam(required = false) Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return commentService.findAllByPostId(postId, page, size);
    }
}
