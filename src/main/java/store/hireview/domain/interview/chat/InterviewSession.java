package store.hireview.domain.interview.chat;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("interview_sessions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSession {
    @Id
    private Long id;
    private String sessionId;
    private Long userId;
    private int currentStep;
    private LocalDateTime createdAt;
    private String jobTitle;
    private String career;
    private String stack;
    private String language;
    private String requestText;
    private SessionType sessionType;
    private boolean enableIceBreaking;
    private boolean enableTailQuestions;

    public InterviewSession(Long userId) {
        this.sessionId = UUID.randomUUID().toString();
        this.userId = userId;
        this.currentStep = 0;
        this.createdAt = LocalDateTime.now();
    }

    public InterviewSession(
            Long userId,
            CreateSessionRequestDTO req
    ) {
        this.sessionId = UUID.randomUUID().toString();
        this.userId = userId;
        this.currentStep = 0;
        this.createdAt = LocalDateTime.now();
        this.jobTitle = req.getJobTitle();
        this.career = req.getCareer();
        this.stack = req.getStack();
        this.language = req.getLanguage();
        this.requestText = req.getRequestText();
        this.enableIceBreaking = req.isEnableIceBreaking();
        this.enableTailQuestions = req.isEnableFollowQuestion();
        this.sessionType = req.getSessionType();
    }

    public void incrementStep() {
        this.currentStep++;
    }
}
