package com.example.eventlottery.domain;

/**
 * Domain model for a single chat message in the AI assistant conversation.
 *
 * Kept in the domain package so that {@link com.example.eventlottery.ui.ChatMessageAdapter}
 * never needs to import {@link com.example.eventlottery.AiAssistantFragment} — matching
 * the same separation used by EventComment, WaitListRecord, etc.
 *
 * @author AI-assisted implementation
 * @version 1.0
 * @see com.example.eventlottery.ui.ChatMessageAdapter
 * @see com.example.eventlottery.AiAssistantFragment
 */
public class ChatMessage {

    private final String  text;
    private final boolean isUser;

    /**
     * @param text   the message content
     * @param isUser true if sent by the user, false if sent by the AI
     */
    public ChatMessage(String text, boolean isUser) {
        this.text   = text;
        this.isUser = isUser;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }
}
