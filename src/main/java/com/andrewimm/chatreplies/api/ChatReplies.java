package com.andrewimm.chatreplies.api;

import java.util.Optional;
import java.util.UUID;

public final class ChatReplies {
    private static volatile ChatRepliesApi api;

    private ChatReplies() {
    }

    public static void setApi(ChatRepliesApi api) {
        ChatReplies.api = api;
    }

    public static Optional<ChatMessageRecord> getMessage(long messageId) {
        return requireApi().getMessage(messageId);
    }

    public static long sendMessage(UUID authorUuid, String authorName, String text, Long replyToMessageId) {
        return requireApi().sendMessage(authorUuid, authorName, text, replyToMessageId);
    }

    public static void registerListener(ChatReplyListener listener) {
        requireApi().registerListener(listener);
    }

    private static ChatRepliesApi requireApi() {
        ChatRepliesApi current = api;
        if (current == null) {
            throw new IllegalStateException("Chat Replies API is not initialized yet");
        }
        return current;
    }
}
