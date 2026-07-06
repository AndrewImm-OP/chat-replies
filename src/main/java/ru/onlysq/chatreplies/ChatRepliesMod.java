package ru.onlysq.chatreplies;

import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.onlysq.chatreplies.api.ChatMessageRecord;
import ru.onlysq.chatreplies.api.ChatReplies;
import ru.onlysq.chatreplies.api.ChatRepliesApi;
import ru.onlysq.chatreplies.api.ChatReplyListener;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public final class ChatRepliesMod implements ModInitializer, ChatRepliesApi {
    public static final String MOD_ID = "chat_replies";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final AtomicLong nextMessageId = new AtomicLong(1);
    private final Map<Long, ChatMessageRecord> messages = new ConcurrentHashMap<>();
    private final Map<UUID, Long> pendingReplies = new ConcurrentHashMap<>();
    private final List<ChatReplyListener> listeners = new CopyOnWriteArrayList<>();
    private volatile MinecraftServer server;

    @Override
    public void onInitialize() {
        ChatReplies.setApi(this);
        ServerLifecycleEvents.SERVER_STARTED.register(startedServer -> server = startedServer);
        ServerLifecycleEvents.SERVER_STOPPING.register(stoppingServer -> server = null);
        registerCommands();
        registerChatHook();
        LOGGER.info("[Chat Replies] Loaded");
    }

    @Override
    public Optional<ChatMessageRecord> getMessage(long messageId) {
        return Optional.ofNullable(messages.get(messageId));
    }

    @Override
    public long sendMessage(UUID authorUuid, String authorName, String text, Long replyToMessageId) {
        ChatMessageRecord record = createRecord(authorUuid, authorName, text, replyToMessageId);
        broadcast(record);
        notifyListeners(record);
        return record.messageId();
    }

    @Override
    public void registerListener(ChatReplyListener listener) {
        listeners.add(listener);
    }

    private void registerChatHook() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            Long replyTo = pendingReplies.remove(sender.getUUID());
            sendMessage(
                    sender.getUUID(),
                    sender.getGameProfile().name(),
                    message.signedContent(),
                    replyTo
            );
            return false;
        });
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                Commands.literal("chatreply")
                        .then(Commands.literal("cancel").executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            pendingReplies.remove(player.getUUID());
                            player.sendSystemMessage(Component.literal("Ответ отменен").withStyle(style -> style.withColor(0xAAAAAA)));
                            return 1;
                        }))
                        .then(Commands.argument("messageId", LongArgumentType.longArg(1)).executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            long messageId = LongArgumentType.getLong(context, "messageId");
                            ChatMessageRecord record = messages.get(messageId);
                            if (record == null) {
                                player.sendSystemMessage(Component.literal("Сообщение не найдено").withStyle(style -> style.withColor(0xFF5555)));
                                return 0;
                            }
                            pendingReplies.put(player.getUUID(), messageId);
                            player.sendSystemMessage(Component.literal("Ответ на " + record.authorName() + ": " + preview(record.plainText()))
                                    .withStyle(style -> style.withColor(0xAAAAAA)));
                            return 1;
                        }))
        ));
    }

    private ChatMessageRecord createRecord(UUID authorUuid, String authorName, String text, Long replyToMessageId) {
        long id = nextMessageId.getAndIncrement();
        ChatMessageRecord record = new ChatMessageRecord(
                id,
                authorUuid,
                authorName,
                text,
                replyToMessageId,
                Instant.now().toEpochMilli()
        );
        messages.put(id, record);
        return record;
    }

    private void broadcast(ChatMessageRecord record) {
        MinecraftServer currentServer = server;
        if (currentServer == null) {
            return;
        }
        Component component = format(record);
        for (ServerPlayer player : PlayerLookup.all(currentServer)) {
            player.sendSystemMessage(component);
        }
    }

    private Component format(ChatMessageRecord record) {
        ChatMessageRecord parent = record.replyToMessageId() == null ? null : messages.get(record.replyToMessageId());
        if (parent == null) {
            return Component.translatable("chat.type.text", author(record.authorName()), clickableBody(record));
        }

        return Component.empty()
                .append(Component.literal("<"))
                .append(author(record.authorName()))
                .append(Component.literal(">"))
                .append(Component.literal("\n↳ " + parent.authorName() + ": " + preview(parent.plainText()))
                        .withStyle(style -> style.withColor(0x8A8A8A)))
                .append(Component.literal("\n"))
                .append(Component.literal("> ").withStyle(style -> style.withColor(0x8A8A8A)))
                .append(clickableBody(record));
    }

    private Component clickableBody(ChatMessageRecord record) {
        return Component.literal(record.plainText()).setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent.RunCommand("/chatreply " + record.messageId()))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Ответить на это сообщение"))));
    }

    private static Component author(String name) {
        int color = 0x55FFFF;
        return Component.literal(name).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
    }

    private void notifyListeners(ChatMessageRecord record) {
        for (ChatReplyListener listener : listeners) {
            try {
                listener.onChatReplyMessage(record);
            } catch (RuntimeException e) {
                LOGGER.warn("Chat reply listener failed", e);
            }
        }
    }

    private static String preview(String text) {
        String normalized = text.replace('\n', ' ').strip();
        if (normalized.length() <= 80) {
            return normalized;
        }
        return normalized.substring(0, 77) + "...";
    }
}
