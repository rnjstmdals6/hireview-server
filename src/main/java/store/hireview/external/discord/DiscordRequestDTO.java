package store.hireview.external.discord;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DiscordRequestDTO {
    private final String content;

    public String toDiscordFormat() {
        return content;
    }
}