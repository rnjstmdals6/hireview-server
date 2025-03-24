package store.hireview.external.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        @JsonProperty("response_mime_type")
        private final String responseMimeType;
        @JsonProperty("response_schema")
        private final ResponseSchema responseSchema;
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