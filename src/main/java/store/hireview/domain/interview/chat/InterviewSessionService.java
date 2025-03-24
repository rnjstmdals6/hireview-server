package store.hireview.domain.interview.chat;

import store.hireview.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class InterviewSessionService {
    private final InterviewSessionRepository sessionRepository;
    private final UserService userService;
    private final ChatService chatService;

    public Mono<InterviewSession> createSession(String email, Long jobId) {
        return userService.findUserByEmail(email)
                .flatMap(user ->
                        sessionRepository.save(new InterviewSession(user.getId()))
                                .flatMap(session ->
                                        chatService.loadQuestionsForSession(session.getSessionId(), jobId)
                                                .then(chatService.saveMessage(session.getSessionId(), "AI",
                                                        String.format("Hello, %s. Welcome to Hireview.", user.getName())))
                                                .then(chatService.saveMessage(session.getSessionId(), "AI",
                                                        "Let's begin your interview. Your interview will consist of 5 questions."))
                                                // getNextQuestion이 Flux<ChatMessage>를 반환하므로 thenMany 사용
                                                .thenMany(chatService.getNextQuestion(session.getSessionId(), 0))
                                                // Flux -> Mono로 합치기 위해 then 사용
                                                .then(Mono.just(session))
                                )
                );
    }


    public Mono<Integer> getCurrentStep(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .map(InterviewSession::getCurrentStep)
                .defaultIfEmpty(0);
    }

    public Mono<Void> incrementStep(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .flatMap(session -> {
                    session.incrementStep();
                    return sessionRepository.save(session);
                })
                .then();
    }

    public Mono<InterviewSession> getSession(String sessionId) {
        return sessionRepository.findBySessionId(sessionId);
    }
}