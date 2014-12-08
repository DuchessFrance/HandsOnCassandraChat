package com.datastax.demo.killrchat.service;

import com.datastax.demo.killrchat.entity.ChatRoomMessage;
import com.datastax.demo.killrchat.exceptions.CannotUpdateMessageException;
import com.datastax.demo.killrchat.model.ChatMessageModel;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private static final Function<ChatRoomMessage, ChatMessageModel> TO_MODEL = new Function<ChatRoomMessage, ChatMessageModel>() {
        @Override
        public ChatMessageModel apply(ChatRoomMessage entity) {
            return entity.toModel();
        }
    };

    @Inject
    PersistenceManager manager;

    public void postNewMessage(String author, String roomName, String messageContent) {
        manager.insert(new ChatRoomMessage(roomName, UUIDs.timeBased(), author, messageContent));
    }

    public void updateLastMessage(String author, String roomName, String newMessageContent) {
        final ChatRoomMessage lastMessage = manager.sliceQuery(ChatRoomMessage.class)
                .forSelect()
                .withPartitionComponents(roomName)
                .getOne();

        if (!lastMessage.getAuthor().equals(author)) {
            throw new CannotUpdateMessageException("Too late, can no longer update your message");
        } else {
            final ChatRoomMessage proxy = manager.forUpdate(ChatRoomMessage.class, new ChatRoomMessage.CompoundPk(roomName, lastMessage.getMessageId()));
            proxy.setContent(newMessageContent);
            manager.update(proxy);
        }

    }

    public List<ChatMessageModel> fetchNextMessagesForRoom(String roomName, UUID fromMessageId, int pageSize) {
        final List<ChatRoomMessage> messages = manager.sliceQuery(ChatRoomMessage.class)
                .forSelect()
                .withPartitionComponents(roomName)
                .fromClusterings(fromMessageId)
                .fromExclusiveToInclusiveBounds()
                .get(pageSize);

        return FluentIterable.from(messages).transform(TO_MODEL).toList().reverse();
    }
}
