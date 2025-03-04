package com.example.hireviewserver.interview.chat;

import com.example.hireviewserver.user.UserService;
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
                .flatMap(user -> sessionRepository.save(new InterviewSession(user.getId()))
                        .flatMap(session ->
                                chatService.loadQuestionsForSession(session.getSessionId(), jobId)
                                        .then(chatService.saveMessage(session.getSessionId(), "AI",
                                                String.format("Hello, %s. Welcome to Hireview.", user.getName())))
                                        .then(chatService.saveMessage(session.getSessionId(), "AI",
                                                "Let's begin your interview."))
                                        .then(chatService.getNextQuestion(session.getSessionId(), 0))
                                        .thenReturn(session)
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