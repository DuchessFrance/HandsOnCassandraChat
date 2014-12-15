package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.entity.ChatRoom;
import com.datastax.demo.killrchat.entity.UserChatRooms;
import com.datastax.demo.killrchat.exceptions.ChatRoomAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.ChatRoomDoesNotExistException;
import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import info.archinnov.achilles.exception.AchillesLightWeightTransactionException;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

import static com.datastax.demo.killrchat.entity.Schema.CHATROOMS;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class ChatRoomService {

    private static final Select SELECT_FIRST_PAGE_FOR_ROOMS = select().from(KEYSPACE, CHATROOMS).limit(bindMarker("fetchSize"));

    @Inject
    PersistenceManager manager;

    private static final Function<ChatRoom, ChatRoomModel> CHAT_ROOM_TO_MODEL = new Function<ChatRoom, ChatRoomModel>() {
        @Override
        public ChatRoomModel apply(ChatRoom entity) {
            return entity.toModel();
        }
    };

    private static final Function<UserChatRooms, LightChatRoomModel> USER_CHAT_ROOMS_TO_MODEL = new Function<UserChatRooms, LightChatRoomModel>() {
        @Override
        public LightChatRoomModel apply(UserChatRooms entity) {
            return entity.toLightModel();
        }
    };

    public void createChatRoom(String roomName, LightUserModel creator) {
        final Set<LightUserModel> participantsList = Sets.newHashSet(creator);
        final String creatorLogin = creator.getLogin();
        final ChatRoom room = new ChatRoom(roomName, creatorLogin, participantsList);
        try {
            manager.insert(room, OptionsBuilder.ifNotExists());
        } catch (AchillesLightWeightTransactionException ex) {
            throw new ChatRoomAlreadyExistsException(format("The room '%s' already exists", roomName));
        }
        manager.insert(new UserChatRooms(creatorLogin, roomName, creatorLogin));
    }

    public ChatRoomModel findRoomByName(String roomName) {
        final ChatRoom chatRoom = manager.find(ChatRoom.class, roomName);
        if (chatRoom == null) {
            throw new ChatRoomDoesNotExistException(format("Chat room '%s' does not exists", roomName));
        }
        return chatRoom.toModel();
    }

    public List<ChatRoomModel> listChatRooms(String fromRoomName, int fetchSize) {
        final Select select;
        final Object[] boundValues;
        if (isBlank(fromRoomName)) {
            select = SELECT_FIRST_PAGE_FOR_ROOMS;
            boundValues = new Object[]{fetchSize};
        } else {
            select = select().from(KEYSPACE, CHATROOMS)
                    .where(gt(token("room_name"), fcall("token", bindMarker("fromRoomName"))))
                    .limit(bindMarker("fetchSize"));
            boundValues = new Object[]{fromRoomName, fetchSize};
        }
        final List<ChatRoom> foundChatRooms = manager.typedQuery(ChatRoom.class, select, boundValues).get();
        return from(foundChatRooms).transform(CHAT_ROOM_TO_MODEL).toList();
    }

    public List<LightChatRoomModel> listChatRoomsForUserByPage(String login, String fromRoomNameExcluding, int fetchSize) {
        final List<UserChatRooms> userChatRooms = manager.sliceQuery(UserChatRooms.class)
                .forSelect()
                .withPartitionComponents(login)
                .fromClusterings(fromRoomNameExcluding)
                .fromExclusiveToInclusiveBounds()
                .get(fetchSize);

        return from(userChatRooms).transform(USER_CHAT_ROOMS_TO_MODEL).toList();
    }

    public void addUserToRoom(LightChatRoomModel chatRoomModel, LightUserModel userModel) {
        final String roomName = chatRoomModel.getRoomName();
        final String newParticipant = userModel.getLogin();
        final String chatRoomCreator = chatRoomModel.getCreator();
        final ChatRoom chatRoomProxy = manager.forUpdate(ChatRoom.class, roomName);
        chatRoomProxy.getParticipants().add(userModel);
        try {
            manager.update(chatRoomProxy, OptionsBuilder.ifEqualCondition("creator", chatRoomCreator));
        } catch (AchillesLightWeightTransactionException ex) {
            throw new ChatRoomDoesNotExistException(format("The chat room '%s' does not exist", roomName));
        }
        manager.insert(new UserChatRooms(newParticipant, roomName, chatRoomCreator));

    }

    public void removeUserFromRoom(LightChatRoomModel chatRoomModel, LightUserModel userModel) {
        final String roomName = chatRoomModel.getRoomName();
        final String participantToBeRemoved = userModel.getLogin();
        final String chatRoomCreator = chatRoomModel.getCreator();
        final ChatRoom chatRoomProxy = manager.forUpdate(ChatRoom.class, roomName);
        chatRoomProxy.getParticipants().remove(userModel);
        try {
            manager.update(chatRoomProxy, OptionsBuilder.ifEqualCondition("creator", chatRoomCreator));
        } catch (AchillesLightWeightTransactionException ex) {
            throw new ChatRoomDoesNotExistException(format("The chat room '%s' does not exist", roomName));
        }
        manager.deleteById(UserChatRooms.class, new UserChatRooms.CompoundPk(participantToBeRemoved, roomName));

        // Remove automatically the room if no participants left
        try {
            manager.deleteById(ChatRoom.class, roomName, OptionsBuilder.ifEqualCondition("participants", null));
        } catch (AchillesLightWeightTransactionException ex) {
        }


    }

}
