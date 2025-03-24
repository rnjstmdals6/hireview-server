package com.example.hireviewserver.domain.interview.chat;

import lombok.Getter;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;

@Getter
public class SessionSinkData {
    private final Sinks.Many<ChatMessage> sink;
    private LocalDateTime lastAccessTime;

    public SessionSinkData() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer(10, false);
        this.lastAccessTime = LocalDateTime.now();
    }

    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }
}