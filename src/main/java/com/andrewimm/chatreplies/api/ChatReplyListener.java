package com.andrewimm.chatreplies.api;

@FunctionalInterface
public interface ChatReplyListener {
    void onChatReplyMessage(ChatMessageRecord message);
}
