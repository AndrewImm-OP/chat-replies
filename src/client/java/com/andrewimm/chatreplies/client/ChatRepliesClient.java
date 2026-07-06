package com.andrewimm.chatreplies.client;

import com.andrewimm.chatreplies.ChatRepliesNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ChatRepliesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                ChatRepliesNetworking.RequiredClientPayload.TYPE,
                (payload, context) -> {
                }
        );
    }
}
