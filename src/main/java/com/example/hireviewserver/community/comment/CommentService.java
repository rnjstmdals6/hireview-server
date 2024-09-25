package com.example.hireviewserver.community.comment;

import com.example.hireviewserver.common.PageResponseDTO;
import com.example.hireviewserver.community.post.Post;
import com.example.hireviewserver.community.post.PostResponseDTO;
import com.example.hireviewserver.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    public Mono<CommentResponseDTO> createComment(CommentRequestDTO dto, Mono<Principal> principal) {
        return principal
                .map(Principal::getName)
                .flatMap(userService::findUserIdByEmail)
                .flatMap(userId -> commentRepository.save(new Comment(dto, userId)))
                .flatMap(comment -> userService.findUserById(comment.getUserId())
                        .map(user -> new CommentResponseDTO(comment, user)));
    }

    public Mono<PageResponseDTO<CommentResponseDTO>> findAllByPostId(Long postId, int page, int size) {
        Mono<Long> total = commentRepository.countByPostId(postId);

        Flux<CommentResponseDTO> comments = commentRepository.findAllByPostIdWithPagination(postId, size, page * size)
                .flatMap(comment -> userService.findUserById(comment.getUserId())
                        .map(user -> new CommentResponseDTO(comment, user)));

        return total.zipWith(comments.collectList(), (totalElements, postList) ->
                new PageResponseDTO<>(postList, totalElements, page));
    }
}
