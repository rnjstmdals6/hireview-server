package com.example.hireviewserver.post;

import com.example.hireviewserver.common.PageResponseDTO;
import com.example.hireviewserver.user.User;
import com.example.hireviewserver.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Mono<PageResponseDTO<PostResponseDTO>> getAllPostsByCategory(String category, int page, int size) {
        Mono<Long> total = postRepository.countByCategory(category);

        Flux<PostResponseDTO> posts = postRepository.findAllByCategoryWithPagination(category, size, page * size)
                .flatMap(post -> userRepository.findById(post.getUserId())
                        .map(user -> new PostResponseDTO(post, user)));

        return total.zipWith(posts.collectList(), (totalElements, postList) ->
                new PageResponseDTO<>(postList, totalElements, page));
    }
}
