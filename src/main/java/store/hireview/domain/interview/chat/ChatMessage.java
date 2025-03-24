package store.hireview.domain.interview.chat;

import lombok.Setter;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("chat_messages")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    @Id
    private Long id;
    private String sessionId;
    private String sender;
    private String content;
    private LocalDateTime createdAt;
    @Setter
    private boolean question;
    public ChatMessage(String sessionId, String sender, String content) {
        this.sessionId = sessionId;
        this.sender = sender;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.question = false;
    }
}
