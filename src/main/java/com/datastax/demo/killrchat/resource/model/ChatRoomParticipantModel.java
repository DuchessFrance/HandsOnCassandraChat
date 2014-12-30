package com.datastax.demo.killrchat.resource.model;

import com.datastax.demo.killrchat.model.LightUserModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoomParticipantModel {

    @NotNull
    private String roomName;

    @NotNull
    private LightUserModel participant;

    public ChatRoomParticipantModel() {
    }

    public ChatRoomParticipantModel(String roomName, LightUserModel participant) {
        this.roomName = roomName;
        this.participant = participant;
    }


    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public LightUserModel getParticipant() {
        return participant;
    }

    public void setParticipant(LightUserModel participant) {
        this.participant = participant;
    }

    public static enum Status {
        JOIN, LEAVE
    }

}
