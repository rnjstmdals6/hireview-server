package store.hireview.domain.interview.chat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("session_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SessionQuestion {
    @Id
    private Long id;
    private String sessionId;
    private String questionText;
    private int step;

    public static SessionQuestion create(String sessionId, String questionText, int step) {
        return new SessionQuestion(null, sessionId, questionText, step);
    }
}