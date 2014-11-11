package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.entity.ChatTheme;
import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.UserModel;
import info.archinnov.achilles.persistence.PersistenceManager;

import javax.inject.Inject;

public class ChatRoomService {

    @Inject
    PersistenceManager manager;

    public void createChatRoom(ChatTheme theme, String name, UserModel ... participants) {

    }

    public void addUserToRoom(ChatRoomModel chatRoomModel, UserModel userModel) {

    }

    public void removeUserFromRoom(ChatRoomModel chatRoomModel, UserModel userModel) {

    }


}
