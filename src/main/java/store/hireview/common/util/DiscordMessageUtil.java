package store.hireview.common.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiscordMessageUtil {

    public static String newUserMessage(String username, String email, long totalUsers) {
        return String.format("""
            👤 **New User Registered**
            -----------------------
            🧑 Username: %s
            📧 Email: %s
            📊 Total Users: %d
            """, username, email, totalUsers);
    }

    public static String feedbackMessage(String title, String sender, String content, String evaluation) {
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
