package com.datastax.demo.killrchat.resource;

import com.datastax.demo.killrchat.exceptions.CannotUpdateMessageException;
import com.datastax.demo.killrchat.model.ChatMessageModel;
import com.datastax.demo.killrchat.resource.model.MessagePaging;
import com.datastax.demo.killrchat.resource.model.MessagePosting;
import com.datastax.demo.killrchat.service.MessageService;
import com.datastax.driver.core.utils.UUIDs;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/messages")
public class MessageResource {

    static final int DEFAULT_MESSAGES_FETCH_SIZE = 10;


    @Inject
    private MessageService service;

    @RequestMapping(value = "/{roomName}", method = POST, consumes = APPLICATION_JSON_VALUE)
    public void postNewMessage(@PathVariable String roomName, @NotNull @RequestBody MessagePosting messagePosting) {
        Validator.validateNotBlank(roomName, "Room name can not be blank for posting new message");
        service.postNewMessage(messagePosting.getAuthor(), roomName, messagePosting.getContent());
    }

    @RequestMapping(value = "/{roomName}", method = PUT, consumes = APPLICATION_JSON_VALUE)
    public void updateLastMessage(@PathVariable String roomName, @NotNull @RequestBody MessagePosting messagePosting) {
        Validator.validateNotBlank(roomName, "Room name can not be blank for updating last message");
        service.updateLastMessage(messagePosting.getAuthor(), roomName, messagePosting.getContent());
    }

    @RequestMapping(value = "/{roomName}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ChatMessageModel> fetchNextMessagesForRoom(@PathVariable String roomName, @RequestParam(required = false) UUID fromMessageId, @RequestParam(required = false) int fetchSize) {
        UUID fromLastMessage = fromMessageId == null ? UUIDs.timeBased() : fromMessageId;
        final int pageSize = fetchSize <= 0 ? DEFAULT_MESSAGES_FETCH_SIZE : fetchSize;
        Validator.validateNotBlank(roomName, "Room name can not be blank for posting new message");
        return service.fetchNextMessagesForRoom(roomName, fromLastMessage, pageSize);
    }

    @ExceptionHandler(value = CannotUpdateMessageException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String messagesResourceException(Exception exception) {
        return exception.getMessage();
    }
}
