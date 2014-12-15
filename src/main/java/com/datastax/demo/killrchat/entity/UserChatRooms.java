package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.google.common.collect.Sets;
import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.type.NamingStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;


import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.USER_CHATROOMS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(keyspace = KEYSPACE, table = USER_CHATROOMS)
@Strategy(naming = NamingStrategy.SNAKE_CASE)
public class UserChatRooms {

    @EmbeddedId
    private CompoundPk primaryKey;

    @Column
    @NotBlank
    private String creator;

    public UserChatRooms(String login, String roomName, String creator) {
        this.primaryKey = new CompoundPk(login, roomName);
        this.creator = creator;
    }

    public String getRoomName() {
        return primaryKey.getRoomName();
    }

    public LightChatRoomModel toLightModel() {
        return new LightChatRoomModel(primaryKey.roomName, creator);
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
        private String roomName;

    }
}
