package com.datastax.demo.killrchat.resource;

import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.model.MessageModel;
import com.datastax.demo.killrchat.resource.model.ChatRoomCreationModel;
import com.datastax.demo.killrchat.resource.model.ChatRoomParticipantModel;
import com.datastax.demo.killrchat.service.ChatRoomService;
import com.datastax.demo.killrchat.service.MessageService;
import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.datastax.demo.killrchat.resource.model.ChatRoomParticipantModel.Status;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/rooms")
public class ChatRoomResource {

    public static final int DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE = 100;

    @Inject
    private ChatRoomService chatRoomService;

    @Inject
    private MessageService messageService;

    @Inject
    private SimpMessagingTemplate template;

    @RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createChatRoom(@NotNull @RequestBody @Valid ChatRoomCreationModel model) {
        final LightUserModel creator = model.getCreator();

        chatRoomService.createChatRoom(model.getRoomName(), model.getBanner(), creator);
    }

    @RequestMapping(value = "/{roomName}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ChatRoomModel findRoomByName(@PathVariable String roomName) {
        return chatRoomService.findRoomByName(roomName);
    }

    @RequestMapping(value = "/list", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ChatRoomModel> listChatRooms(@RequestParam(required = false) int fetchSize) {
        final int pageSize = fetchSize <= 0 ? DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE : fetchSize;
        return chatRoomService.listChatRooms(pageSize);
    }

    @RequestMapping(value = "/user", method = PUT, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addUserToChatRoom(@NotNull @RequestBody @Valid ChatRoomParticipantModel model) {
        final String roomName = model.getRoomName();
        final LightUserModel participant = model.getParticipant();

        chatRoomService.addUserToRoom(roomName, participant);
        final MessageModel joiningMessage = messageService.createJoiningMessage(roomName, participant);
        template.convertAndSend("/topic/participants/"+ roomName, participant, ImmutableMap.<String,Object>of("status", Status.JOIN));
        template.convertAndSend("/topic/messages/"+roomName, joiningMessage);
    }

    @RequestMapping(value = "/user/remove", method = PUT, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUserFromChatRoom(@NotNull @RequestBody @Valid ChatRoomParticipantModel model) {
        final String roomName = model.getRoomName();
        final LightUserModel participant = model.getParticipant();

        chatRoomService.removeUserFromRoom(roomName, participant);
        final MessageModel leavingMessage = messageService.createLeavingMessage(roomName, participant);
        template.convertAndSend("/topic/participants/"+ roomName, participant, ImmutableMap.<String,Object>of("status", Status.LEAVE));
        template.convertAndSend("/topic/messages/"+roomName, leavingMessage);
    }
}
