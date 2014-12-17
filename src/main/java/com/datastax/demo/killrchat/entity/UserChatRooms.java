package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.type.NamingStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.validation.constraints.NotNull;

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
    @NotNull
    @JSON
    private LightUserModel creator;

    public UserChatRooms(String login, String roomName, LightUserModel creator) {
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
        private String login;

        @ClusteringColumn
        private String roomName;

    }
}
