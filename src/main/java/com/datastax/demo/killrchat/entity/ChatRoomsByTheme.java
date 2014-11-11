package com.datastax.demo.killrchat.entity;

import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.type.NamingStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static com.datastax.demo.killrchat.entity.Schema.CHATROOMS_BY_THEME;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(keyspace = KEYSPACE, table = CHATROOMS_BY_THEME)
@Strategy(naming = NamingStrategy.SNAKE_CASE)
public class ChatRoomsByTheme extends AbstractChatRoom {

    @EmbeddedId
    private CompoundPk primaryKey;


    public ChatRoomsByTheme(ChatTheme theme, UUID roomId, String roomName) {
        this.primaryKey = new CompoundPk(theme, roomId);
        this.roomName = roomName;
    }

    @Override
    public UUID getRoomId() {
        return primaryKey.roomId;
    }

    @Override
    public ChatTheme getTheme() {
        return primaryKey.theme;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompoundPk {

        @PartitionKey
        @Order(1)
        private ChatTheme theme;

        @Column
        @Order(2)
        private UUID roomId;

    }
}
