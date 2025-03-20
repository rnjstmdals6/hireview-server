package com.example.hireviewserver.gemini;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class GeminiStructuredRequestDTO {

    private final List<Content> contents;
    private final GenerationConfig generationConfig;

    @Getter
    @Builder
    public static class Content {
        private final String role;
        private final List<Part> parts;

        @Getter
        @Builder
        public static class Part {
            private final String text;
        }
    }
    @Getter
    @Builder
    public static class GenerationConfig {
        private final String response_mime_type;
        private final ResponseSchema response_schema;
    }

    @Getter
    @Builder
    public static class ResponseSchema {
        private final String type;
        private final ResponseSchemaItems items;
        private final Map<String, ResponseSchemaProperty> properties;
    }

    @Getter
    @Builder
    public static class ResponseSchemaItems {
        private final String type;
        private final Map<String, ResponseSchemaProperty> properties;
    }

    @Getter
    @Builder
    public static class ResponseSchemaProperty {
        private String type;
        private String format;
        private Map<String, ResponseSchemaProperty> properties;
        private ResponseSchemaProperty items;
    }
}