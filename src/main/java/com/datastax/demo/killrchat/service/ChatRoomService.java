package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.entity.ChatRooms;
import com.datastax.demo.killrchat.entity.UserChatRooms;
import com.datastax.demo.killrchat.exceptions.ChatRoomAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.ChatRoomDoesNotExistException;
import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.driver.core.SimpleStatement;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import info.archinnov.achilles.exception.AchillesLightWeightTransactionException;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.Options;
import info.archinnov.achilles.type.OptionsBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.*;

import static com.datastax.demo.killrchat.entity.Schema.CHATROOMS;
import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.driver.core.querybuilder.QueryBuilder.gt;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.querybuilder.QueryBuilder.token;
import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;

public class ChatRoomService {

    @Inject
    PersistenceManager manager;

    int roomFetchPage;

    private static final Function<ChatRooms, LightChatRoomModel> CHAT_ROOMS_TO_MODEL = new Function<ChatRooms, LightChatRoomModel>() {
        @Override
        public LightChatRoomModel apply(ChatRooms entity) {
            return entity.toLightModel();
        }
    };

    private static final Function<UserChatRooms, LightChatRoomModel> USER_CHAT_ROOMS_TO_MODEL = new Function<UserChatRooms, LightChatRoomModel>() {
        @Override
        public LightChatRoomModel apply(UserChatRooms entity) {
            return entity.toLightModel();
        }
    };

    public void createChatRoom(String roomName, LightUserModel creator, boolean directChat, boolean privateRoom) {
        final Set<LightUserModel> participantsList = Sets.newHashSet(creator);
        final String creatorLogin = creator.getLogin();
        final ChatRooms room = new ChatRooms(roomName, directChat, privateRoom, creatorLogin, participantsList);
        try {
            manager.insert(room, OptionsBuilder.ifNotExists());
        } catch (AchillesLightWeightTransactionException ex) {
            throw new ChatRoomAlreadyExistsException(format("The room '%s' already exists", roomName));
        }
        manager.insert(new UserChatRooms(creatorLogin, roomName, creatorLogin));
    }

    public List<LightChatRoomModel> listChatRooms(String fromRoomName, int limit) {
        String fromValue = StringUtils.isBlank(fromRoomName) ? "''": fromRoomName;
        SimpleStatement statement = new SimpleStatement("SELECT * FROM "+KEYSPACE+"."+CHATROOMS+" WHERE token(room_name) > token("+fromValue+") LIMIT "+limit);

        final List<ChatRooms> foundChatRooms = manager.typedQuery(ChatRooms.class, statement).get();
        return from(foundChatRooms).transform(CHAT_ROOMS_TO_MODEL).toList();
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
        final ChatRooms chatRoomProxy = manager.forUpdate(ChatRooms.class, roomName);
        chatRoomProxy.getParticipants().add(userModel);
        try {
            manager.update(chatRoomProxy, OptionsBuilder.ifConditions(new Options.LWTCondition("creator", chatRoomCreator)));
            manager.insert(new UserChatRooms(newParticipant, roomName, chatRoomCreator));
        } catch (AchillesLightWeightTransactionException ex) {
            throw new ChatRoomDoesNotExistException(format("The chat room '%s' does not exist", roomName));
        }

    }

    public void removeUserFromRoom(LightChatRoomModel chatRoomModel, LightUserModel userModel) {
        final String roomName = chatRoomModel.getRoomName();
        final String participantToBeRemoved = userModel.getLogin();
        final String chatRoomCreator = chatRoomModel.getCreator();
        final ChatRooms chatRoomProxy = manager.forUpdate(ChatRooms.class, roomName);
        chatRoomProxy.getParticipants().remove(userModel);
        try {
            manager.update(chatRoomProxy, OptionsBuilder.ifConditions(new Options.LWTCondition("creator", chatRoomCreator)));
            manager.deleteById(UserChatRooms.class, new UserChatRooms.CompoundPk(participantToBeRemoved, roomName));
        } catch (AchillesLightWeightTransactionException ex) {
            throw new ChatRoomDoesNotExistException(format("The chat room '%s' does not exist", roomName));
        }

    }

}
