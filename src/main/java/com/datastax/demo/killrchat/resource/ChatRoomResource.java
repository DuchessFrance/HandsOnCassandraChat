package com.datastax.demo.killrchat.resource;

import com.datastax.demo.killrchat.exceptions.*;
import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.resource.model.ChatRoomCreationModel;
import com.datastax.demo.killrchat.resource.model.ChatRoomParticipantModel;
//import com.datastax.demo.killrchat.resource.model.PagingByToken;
import com.datastax.demo.killrchat.service.ChatRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/rooms")
public class ChatRoomResource {

    public static final int DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE = 10;

    @Inject
    private ChatRoomService service;

    @RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE)
    public void createChatRoom(@NotNull @RequestBody @Valid ChatRoomCreationModel model) {
        final String roomName = model.getRoomName();
        final LightUserModel creator = model.getCreator();

        service.createChatRoom(roomName, creator);
    }

    @RequestMapping(value = "/{roomName}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ChatRoomModel findRoomByName(@PathVariable String roomName) {
        return service.findRoomByName(roomName);
    }

    @RequestMapping(value = "/list", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ChatRoomModel> listChatRooms(@RequestParam(required = false) String fromRoomName, @RequestParam(required = false) int fetchSize) {
        final int pageSize = fetchSize <= 0 ? DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE : fetchSize;
        return service.listChatRooms(fromRoomName, pageSize);
    }

    @RequestMapping(value = "/user", method = PUT, consumes = APPLICATION_JSON_VALUE)
    public void addUserToChatRoom(@NotNull @RequestBody @Valid ChatRoomParticipantModel model) {
        service.addUserToRoom(model.getRoom(), model.getParticipant());
    }

    @RequestMapping(value = "/user/remove", method = PUT, consumes = APPLICATION_JSON_VALUE)
    public void removeUserFromChatRoom(@NotNull @RequestBody @Valid ChatRoomParticipantModel model) {
        service.removeUserFromRoom(model.getRoom(), model.getParticipant());
    }

    @ExceptionHandler(value = {
            ChatRoomAlreadyExistsException.class,
            ChatRoomDoesNotExistException.class
    })
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String chatRoomResourceException(Exception exception) {
        return exception.getMessage();
    }
}
