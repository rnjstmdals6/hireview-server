package store.hireview.domain.interview.chat;
import lombok.Data;

@Data
public class CreateSessionRequestDTO {
    private String jobTitle;
    private String career;
    private String stack;
    private String language;
    private String requestText;
    private boolean enableIceBreaking;
    private boolean enableFollowQuestion;
    private SessionType sessionType;
}
