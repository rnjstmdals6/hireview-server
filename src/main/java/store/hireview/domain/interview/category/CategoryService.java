package store.hireview.domain.interview.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Mono<List<CategoryResponseDTO>> getAllCategories() {
        return categoryRepository.findAll()
                .map(category -> new CategoryResponseDTO(category.getId(), category.getName()))
                .collectList();
    }
}
