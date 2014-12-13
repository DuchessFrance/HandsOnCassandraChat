package com.datastax.demo.killrchat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChatRoomModel extends LightChatRoomModel{

    @JsonProperty("private-room")
    private boolean privateRoom;

    @JsonProperty("direct-chat")
    private boolean directChat;

    private Set<LightUserModel> participants;

    private String banner;

    public ChatRoomModel(String roomName, String creator, String banner, boolean privateRoom, boolean directChat, Set<LightUserModel> participants) {
        super(roomName, creator);
        this.banner = banner;
        this.privateRoom = privateRoom;
        this.directChat = directChat;
        this.participants = participants;
    }
}
