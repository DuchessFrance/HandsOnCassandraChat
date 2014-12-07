package com.datastax.demo.killrchat.resource;

import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.service.ChatRoomService;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.List;

public class ChatRoomResource {

    static final int DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE = 10;

    @Inject
    private ChatRoomService service;

    public void createChatRoom(String roomName, LightUserModel creator, boolean directChat, boolean privateRoom) {
        Validator.validateNotBlank(roomName,"Room name should not be blank for chat room creation");
        service.createChatRoom(roomName, creator, directChat, privateRoom);
    }

    public List<LightChatRoomModel> listChatRooms(String fromRoomName, int limit) {
        final String fromRoom = StringUtils.isBlank(fromRoomName) ? "" : fromRoomName;
        final int pageSize = limit <= 0 ? DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE :limit;
        return service.listChatRooms(fromRoom, pageSize);
    }

    public List<LightChatRoomModel> listChatRoomsForUserByPage(String login, String fromRoomNameExcluding, int fetchSize) {
        Validator.validateNotBlank(login, "Missing login for user chat rooms listing");
        final String fromRoom = StringUtils.isBlank(fromRoomNameExcluding) ? "" : fromRoomNameExcluding;
        final int pageSize = fetchSize <= 0 ? DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE :fetchSize;
        return service.listChatRoomsForUserByPage(login, fromRoom, pageSize);
    }

    public void addUserToChatRoom(LightChatRoomModel chatRoomModel, LightUserModel userModel) {
        service.addUserToRoom(chatRoomModel, userModel);
    }

    public void removeUserFromChatRoom(LightChatRoomModel chatRoomModel, LightUserModel userModel) {
        service.removeUserFromRoom(chatRoomModel, userModel);
    }

}
