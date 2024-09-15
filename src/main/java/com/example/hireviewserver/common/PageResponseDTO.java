package com.example.hireviewserver.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PageResponseDTO<T> {
    private List<T> content;
    private long totalElements;
    private long currentPage;

    public PageResponseDTO(List<T> content, long totalElements, long currentPage) {
        this.content = content;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
    }
}