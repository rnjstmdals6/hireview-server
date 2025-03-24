package store.hireview.domain.community.like;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/api/v1/like")
    public Mono<Void> addOrDeleteLike(Mono<Principal> principal, @RequestParam Long postId) {
        return likeService.addOrDeleteLike(postId, principal);
    }
}
