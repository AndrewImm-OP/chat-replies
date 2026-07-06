package com.andrewimm.chatreplies.api;

import java.util.UUID;

public record ChatMessageRecord(
        long messageId,
        UUID authorUuid,
        String authorName,
        String plainText,
        Long replyToMessageId,
        long createdAt
) {
}
