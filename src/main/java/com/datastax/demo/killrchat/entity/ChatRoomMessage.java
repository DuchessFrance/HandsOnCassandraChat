package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.ChatMessageModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.driver.core.utils.UUIDs;
import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.type.NamingStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.datastax.demo.killrchat.entity.Schema.CHATROOM_MESSAGES;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(keyspace = KEYSPACE, table = CHATROOM_MESSAGES)
@Strategy(naming = NamingStrategy.SNAKE_CASE)
public class ChatRoomMessage {

    @EmbeddedId
    private CompoundPk primaryKey;

    @NotNull
    @JSON
    @Column
    private LightUserModel author;

    @NotEmpty
    @Column
    private String content;

    @Column
    private boolean systemMessage;

    public ChatRoomMessage(String roomName, UUID messageId, LightUserModel author, String content) {
        this.primaryKey = new CompoundPk(roomName, messageId);
        this.author = author;
        this.content = content;
        this.systemMessage = false;
    }


    public String getRoomName() {
        return primaryKey.roomName;
    }

    public UUID getMessageId() {
        return primaryKey.messageId;
    }

    public ChatMessageModel toModel() {
        ChatMessageModel model = new ChatMessageModel();
        model.setAuthor(this.author);
        model.setContent(this.content);
        model.setMessageId(this.getMessageId());
        model.setSystemMessage(this.systemMessage);
        model.setCreationDate(new Date(UUIDs.unixTimestamp(this.getMessageId())));
        return model;
    }

    public static ChatRoomMessage fromModel(String roomName, ChatMessageModel model) {
        final ChatRoomMessage entity = new ChatRoomMessage();
        entity.setPrimaryKey(new CompoundPk(roomName, model.getMessageId()));
        entity.setAuthor(model.getAuthor());
        entity.setContent(model.getContent());
        entity.setSystemMessage(model.isSystemMessage());
        return entity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompoundPk {

        @PartitionKey
        @Order(1)
        private String roomName;

        @Order(value = 2, reversed = true)
        @Column
        @TimeUUID
        private UUID messageId;
    }

}
