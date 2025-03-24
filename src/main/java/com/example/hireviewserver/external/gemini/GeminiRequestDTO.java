package com.example.hireviewserver.external.gemini;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GeminiRequestDTO {
    private final List<Content> contents;

    @Getter
    @Builder
    public static class Content {
        private final List<ContentPart> parts;
        private final String role;

        @Getter
        @Builder
        public static class ContentPart {
            private final String text;
        }
    }
}
