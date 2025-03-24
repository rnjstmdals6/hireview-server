package store.hireview.domain.community.comment;

import store.hireview.common.response.PageResponse;
import store.hireview.domain.user.UserService;
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

    public Mono<PageResponse<CommentResponseDTO>> findAllByPostId(Long postId, int page, int size) {
        Mono<Long> total = getCommentCount(postId);

        Flux<CommentResponseDTO> comments = commentRepository.findAllByPostIdWithPagination(postId, size, page * size)
                .flatMap(comment -> userService.findUserById(comment.getUserId())
                        .map(user -> new CommentResponseDTO(comment, user)));

        return total.zipWith(comments.collectList(), (totalElements, postList) ->
                new PageResponse<>(postList, totalElements, page));
    }

    public Mono<Long> getCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    public Mono<Void> deleteComment(Long commentId, String email) {
        return userService.findUserIdByEmail(email).flatMap(
                userId -> commentRepository.deleteByIdAndUserId(commentId, userId)
        );
    }

    public Mono<CommentResponseDTO> modifyComment(CommentRequestDTO dto, Mono<Principal> principal, Long commentId) {
        return principal
                .map(Principal::getName)
                .flatMap(userService::findUserIdByEmail)
                .flatMap(userId -> commentRepository.findById(commentId)
                        .flatMap(comment -> {
                            if (!comment.getUserId().equals(userId)) {
                                return Mono.error(new RuntimeException("권한이 없습니다."));
                            }
                            comment.modify(dto);
                            return commentRepository.save(comment)
                                    .flatMap(updateComment -> userService.findUserById(updateComment.getUserId())
                                            .map(user -> new CommentResponseDTO(updateComment, user)));
                        })
                );
    }
}
