package com.datastax.demo.killrchat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoomModel extends LightChatRoomModel{

    private Set<LightUserModel> participants;

    public ChatRoomModel(String roomName, String creator, Set<LightUserModel> participants) {
        super(roomName, creator);
        this.participants = participants;
    }
}
