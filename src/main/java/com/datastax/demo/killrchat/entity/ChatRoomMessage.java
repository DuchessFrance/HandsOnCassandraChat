package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.LightUserModel;
import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.type.NamingStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.cassandra.utils.UUIDGen;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Arrays;
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

    @NotEmpty
    @Column
    private String author;

    @NotEmpty
    @Column
    private String content;

    @Column
    private boolean systemMessage;

    public ChatRoomMessage(String roomName, UUID messageId, String author, String content) {
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompoundPk {

        @PartitionKey
        @Order(1)
        private String roomName;

        @Order(2)
        @Column
        @TimeUUID
        private UUID messageId;
    }

}
