package com.datastax.demo.killrchat.resource.model;

import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomParticipantModel {

    @NotNull
    private LightChatRoomModel room;

    @NotNull
    private LightUserModel participant;

}
