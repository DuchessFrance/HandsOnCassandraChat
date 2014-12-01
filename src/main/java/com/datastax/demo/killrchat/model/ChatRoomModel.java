package com.datastax.demo.killrchat.model;

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

    private boolean privateRoom;

    private boolean directChat;

    private Set<LightUserModel> participants;

    private String banner;

}
