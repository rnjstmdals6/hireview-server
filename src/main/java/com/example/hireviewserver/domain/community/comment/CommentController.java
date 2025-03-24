package com.example.hireviewserver.domain.community.comment;

import com.example.hireviewserver.common.response.PageResponse;
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
    public Mono<PageResponse<CommentResponseDTO>> getAllComment(
            @RequestParam(required = false) Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return commentService.findAllByPostId(postId, page, size);
    }

    @PutMapping("/api/v1/comment/{commentId}")
    public Mono<CommentResponseDTO> modifyComment(Mono<Principal> principal, @PathVariable Long commentId, @RequestBody CommentRequestDTO dto) {
        return commentService.modifyComment(dto, principal, commentId);
    }

    @DeleteMapping("/api/v1/comment/{commentId}")
    public Mono<Void> deleteComment(@PathVariable Long commentId, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(email -> commentService.deleteComment(commentId, email));
    }
}
