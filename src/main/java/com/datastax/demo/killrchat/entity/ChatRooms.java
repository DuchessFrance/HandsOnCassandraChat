package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.type.NamingStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;
import java.util.Set;

import static com.datastax.demo.killrchat.entity.Schema.CHATROOMS;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(keyspace = KEYSPACE, table = CHATROOMS)
@Strategy(naming = NamingStrategy.SNAKE_CASE)
public class ChatRooms {

    @Id
    private String roomName;

    @Column
    private boolean privateRoom;

    @Column
    private boolean directChat;

    @Column
    private String banner;

    @Column
    @NotBlank
    private String creator;

    @Column
    @JSON
    private Set<LightUserModel> participants;

    public ChatRooms(String roomName, boolean privateRoom, boolean directChat, String creator,Set<LightUserModel> participants) {
        this.roomName = roomName;
        this.privateRoom = privateRoom;
        this.directChat = directChat;
        this.creator = creator;
        this.participants = participants;
    }

    public LightChatRoomModel toLightModel() {
        return new LightChatRoomModel(roomName, creator);
    }
}
