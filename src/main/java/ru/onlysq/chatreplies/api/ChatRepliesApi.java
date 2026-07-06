package ru.onlysq.chatreplies.api;

import java.util.Optional;
import java.util.UUID;

public interface ChatRepliesApi {
    Optional<ChatMessageRecord> getMessage(long messageId);

    long sendMessage(UUID authorUuid, String authorName, String text, Long replyToMessageId);

    void registerListener(ChatReplyListener listener);
}
