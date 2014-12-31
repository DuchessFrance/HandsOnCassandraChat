package com.datastax.demo.killrchat.resource.model;

import com.datastax.demo.killrchat.model.LightUserModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoomCreationModel {

    @Pattern(regexp="[a-zA-Z0-9][a-zA-Z0-9_.-]{2,30}")
    private String roomName;
    
    private String banner;

    @NotNull
    private LightUserModel creator;

    public ChatRoomCreationModel() {
    }

    public ChatRoomCreationModel(String roomName, String banner, LightUserModel creator) {
        this.roomName = roomName;
        this.banner = banner;
        this.creator = creator;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public LightUserModel getCreator() {
        return creator;
    }

    public void setCreator(LightUserModel creator) {
        this.creator = creator;
    }
}
