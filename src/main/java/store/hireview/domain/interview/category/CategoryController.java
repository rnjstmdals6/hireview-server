package store.hireview.domain.interview.category;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/api/v1/category/all")
    @Operation(summary = "서버에 존재하는 모든 카테고리를 불러옵니다.")
    public Mono<List<CategoryResponseDTO>> getAllCategory() {
        return categoryService.getAllCategories();
    }
}
