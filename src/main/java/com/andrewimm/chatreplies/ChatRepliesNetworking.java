package com.andrewimm.chatreplies;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class ChatRepliesNetworking {
    public static final RequiredClientPayload REQUIRED_CLIENT_PAYLOAD = new RequiredClientPayload();

    private ChatRepliesNetworking() {
    }

    public record RequiredClientPayload() implements CustomPacketPayload {
        public static final Type<RequiredClientPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(ChatRepliesMod.MOD_ID, "required_client"));

        public static final StreamCodec<RegistryFriendlyByteBuf, RequiredClientPayload> CODEC =
                StreamCodec.unit(REQUIRED_CLIENT_PAYLOAD);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
