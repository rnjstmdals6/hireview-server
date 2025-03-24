package com.example.hireviewserver.domain.interview.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponseDTO {
    private String sessionId;
    private String sender;
    private String content;
    private boolean question;

    public static ChatMessageResponseDTO from(ChatMessage chatMessage, boolean isAI) {
        boolean question = isAI && chatMessage.isQuestion();
        return new ChatMessageResponseDTO(
                chatMessage.getSessionId(),
                chatMessage.getSender(),
                chatMessage.getContent(),
                question
        );
    }
}
