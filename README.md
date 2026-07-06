# Chat Replies

Мод добавляет ответы на сообщения в Minecraft-чате.

Сценарий простой: игрок кликает по тексту сообщения, пишет следующее сообщение как обычно, а сервер показывает его как ответ. Клик вешается именно на текст сообщения, не на ник, поэтому ванильное поведение ника вроде `/tell <игрок>` не перехватывается.

## Установка

Нужны:

- Minecraft `26.1.x`;
- Fabric Loader `0.19.3` или новее;
- Fabric API;
- Java 25 или новее.

Собери jar:

```bash
JAVA_HOME=/usr/lib/jvm/java-25-openjdk ./gradlew build
```

Положи файл из `build/libs/` в папку `mods/` сервера:

```text
build/libs/chat-replies-<version>.jar
```

После этого перезапусти сервер.

## Как пользоваться

Обычное сообщение выглядит как обычный чат Minecraft. Мод не собирает его вручную через `<ник> текст`, а использует стандартный `chat.type.text`, чтобы не ломать ресурспаки и моды, которые меняют вид обычного чата.

Чтобы ответить:

1. Наведи курсор на текст сообщения.
2. Появится подсказка `Ответить на это сообщение`.
3. Кликни по тексту.
4. Напиши следующее сообщение в чат.

После клика игрок видит только локальное подтверждение:

```text
Ответ на Darios8642: бля
```

Ответ отображается так:

```text
<Time_Conserva>
↳ Darios8642: бля
> как будто звучит полезно
```

Если ответ выбран случайно:

```text
/chatreply cancel
```

## Команды

```text
/chatreply <messageId>
/chatreply cancel
```

`/chatreply <messageId>` обычно не вводят руками. Minecraft click event запускает эту команду сам, когда игрок кликает по тексту сообщения.

`/chatreply cancel` очищает выбранный ответ для игрока.

## Как это работает

Когда игрок отправляет сообщение, сервер создаёт запись:

```java
ChatMessageRecord {
    long messageId;
    UUID authorUuid;
    String authorName;
    String plainText;
    Long replyToMessageId;
    long createdAt;
}
```

Если игрок до этого выбрал сообщение для ответа, его ID лежит во временной карте `pendingReplies`. Следующее сообщение получает `replyToMessageId`, после чего pending-запись удаляется.

История сообщений хранится только в памяти сервера. После рестарта старые `messageId` пропадают. Сейчас это чатовая механика, не архив и не логгер.

## API для других модов

Пакет:

```java
com.andrewimm.chatreplies.api
```

Основные методы:

```java
Optional<ChatMessageRecord> message = ChatReplies.getMessage(messageId);

long newMessageId = ChatReplies.sendMessage(
        authorUuid,
        authorName,
        text,
        replyToMessageId
);

ChatReplies.registerListener(message -> {
    // вызовется на каждое сообщение, обработанное Chat Replies
});
```

`replyToMessageId` равен `null`, если это обычное сообщение. Если это ответ, там будет `messageId` исходного сообщения.

## Ограничения

- Сообщения не сохраняются на диск.
- Мод перехватывает серверный чат и рассылает свой `Component`. В этой версии Fabric API на сервере есть allow/observe события, но нет нормального hook-а “измени сообщение перед отправкой”.
- Reply-блок имеет собственный фиксированный формат. Обычные сообщения остаются на стандартном `chat.type.text`.

## Разработка

Сборка:

```bash
JAVA_HOME=/usr/lib/jvm/java-25-openjdk ./gradlew build
```

Основной класс:

```text
src/main/java/com/andrewimm/chatreplies/ChatRepliesMod.java
```

API:

```text
src/main/java/com/andrewimm/chatreplies/api/
```
