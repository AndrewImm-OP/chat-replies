# Chat Replies

Fabric-мод для ответов на сообщения в Minecraft-чате.

## Что делает

- хранит серверные `messageId` для сообщений;
- позволяет выбрать сообщение кликом по тексту сообщения;
- следующее сообщение игрока отправляется как reply;
- форматирует ответы отдельным блоком:

```text
<Time_Conserva>
↳ Darios8642: бля
> как будто звучит полезно
```

Обычные сообщения рендерятся через vanilla translation key `chat.type.text`, а не через вручную собранные угловые скобки. Поэтому ресурспаки/моды, которые меняют стандартный вид чата с `<name>` на `[name]`, не ломают обычный чат. Reply-блок намеренно имеет свой формат, чтобы он всегда читался одинаково.

## Требования

- Fabric Loader `>=0.19.3`
- Minecraft `26.1.x`
- Java `>=25`
- Fabric API

Версии задаются в `gradle.properties`.

## Команды

```text
/chatreply <messageId>
/chatreply cancel
```

`/chatreply <messageId>` обычно вызывается кликом по тексту сообщения. Клик по нику не трогается.

## Public API

Пакет API:

```java
com.andrewimm.chatreplies.api
```

Основные вызовы:

```java
Optional<ChatMessageRecord> getMessage(long messageId);

long sendMessage(UUID authorUuid, String authorName, String text, Long replyToMessageId);

void registerListener(ChatReplyListener listener);
```

Событие:

```java
void onChatReplyMessage(ChatMessageRecord message);
```

`ChatMessageRecord.replyToMessageId()` содержит ID исходного сообщения, если сообщение является ответом.

## Сборка

```bash
JAVA_HOME=/usr/lib/jvm/java-25-openjdk ./gradlew build
```

Готовый jar появляется в:

```text
build/libs/chat-replies-<version>.jar
```

## Публичный репозиторий

Этот каталог рассчитан на отдельный GitHub-репозиторий, например:

```text
chat-replies
```
