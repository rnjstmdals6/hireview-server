package store.hireview.common.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private long currentPage;

    public PageResponse(List<T> content, long totalElements, long currentPage) {
        this.content = content;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
    }
}