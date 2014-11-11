package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.UserModel;
import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.type.NamingStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.USER_CHATROOMS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(keyspace = KEYSPACE, table = USER_CHATROOMS)
@Strategy(naming = NamingStrategy.SNAKE_CASE)
public class UserChatRooms extends AbstractChatRoom {

    @EmbeddedId
    private CompoundPk primaryKey;

    @Column
    private ChatTheme theme;

    @Override
    public UUID getRoomId() {
        return primaryKey.roomId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompoundPk {

        @PartitionKey
        @Order(1)
        private String login;

        @Column
        @Order(2)
        private UUID roomId;

    }
}
