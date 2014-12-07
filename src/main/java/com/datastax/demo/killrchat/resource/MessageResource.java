package com.datastax.demo.killrchat.resource;

import com.datastax.demo.killrchat.model.ChatMessageModel;
import com.datastax.demo.killrchat.service.MessageService;
import com.datastax.driver.core.utils.UUIDs;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

public class MessageResource {

    static final int DEFAULT_MESSAGES_FETCH_SIZE = 10;


    @Inject
    private MessageService service;

    public void postNewMessage(String author, String roomName, String messageContent) {
        Validator.validateNotBlank(author, "Author can not be blank for posting new message");
        Validator.validateNotBlank(roomName, "Room name can not be blank for posting new message");
        Validator.validateNotBlank(messageContent, "Message can not be blank for posting new message");
        service.postNewMessage(author, roomName, messageContent);
    }

    public void updateLastMessage(String author, String roomName, String newMessageContent) {
        Validator.validateNotBlank(author, "Author can not be blank for updating last message");
        Validator.validateNotBlank(roomName, "Room name can not be blank for updating last message");
        Validator.validateNotBlank(newMessageContent, "New message can not be blank for updating last message");
        service.updateLastMessage(author, roomName, newMessageContent);
    }

    public List<ChatMessageModel> fetchNextMessagesForRoom(String roomName, UUID fromMessageId, int pageSize) {
        UUID fromLastMessage = fromMessageId == null ? UUIDs.timeBased(): fromMessageId;
        final int fetchSize = pageSize <= 0 ? DEFAULT_MESSAGES_FETCH_SIZE :pageSize;
        Validator.validateNotBlank(roomName, "Room name can not be blank for posting new message");
        return service.fetchNextMessagesForRoom(roomName, fromLastMessage, fetchSize);
    }
}
