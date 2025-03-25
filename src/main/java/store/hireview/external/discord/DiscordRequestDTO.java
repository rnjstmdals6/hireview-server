package store.hireview.external.discord;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DiscordRequestDTO {
    private final String title;
    private final String sender;
    private final String content;
    private final String evaluation;

    public String toDiscordFormat() {
        return String.format("""
                📬 %s from **%s**
                -----------------------
                💬 **Suggestion**
                ```
                %s
                ```
                🧠 **Evaluation**
                ```
                %s
                ```
                """, title, sender, content, evaluation);
    }
}