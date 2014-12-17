package com.datastax.demo.killrchat.service;

import static com.datastax.demo.killrchat.entity.Schema.*;
import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.assertj.core.api.Assertions.*;

import com.datastax.demo.killrchat.entity.User;
import com.datastax.demo.killrchat.model.ChatMessageModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import info.archinnov.achilles.junit.AchillesResource;
import info.archinnov.achilles.junit.AchillesResourceBuilder;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {

    @Rule
    public AchillesResource resource = AchillesResourceBuilder
            .withEntityPackages(User.class.getPackage().getName())
            .withKeyspaceName(KEYSPACE)
            .withBeanValidation()
            .tablesToTruncate(CHATROOM_MESSAGES)
            .truncateBeforeAndAfterTest().build();

    private PersistenceManager manager = resource.getPersistenceManager();

    private Session session = resource.getNativeSession();

    private MessageService service = new MessageService();

    private LightUserModel johnDoe = new LightUserModel("jdoe","John","DOE");

    private LightUserModel helenSue = new LightUserModel("hsue","Helen","SUE");

    @Before
    public void setUp() {
        service.manager = this.manager;
    }

    @Test
    public void should_create_new_chat_message() throws Exception {
        //Given
        LightUserModel johnDoe = new LightUserModel("jdoe","John","DOE");
        String roomName = "games";
        String messageContent = "Starcraft2 is awesome!";

        //When
        service.postNewMessage(johnDoe, roomName, messageContent);

        //Then
        final Select selectMessages = select().from(KEYSPACE, CHATROOM_MESSAGES)
                .where(eq("room_name", roomName))
                .limit(1);

        final Row lastMessage = session.execute(selectMessages).one();

        assertThat(lastMessage.getUUID("message_id")).isNotNull();
        assertThat(lastMessage.getString("author")).contains(manager.serializeToJSON(johnDoe));
        assertThat(lastMessage.getString("content")).isEqualTo(messageContent);
        assertThat(lastMessage.getBool("system_message")).isFalse();
    }


    @Test
    public void should_fetch_next_messages_starting_from_now() throws Exception {
        //Given
        String roomName = "games";
        String message1 = "Starcraft2 is awesome!";
        String message2 = "No, WoW is ways better";
        String message3 = "Ok, so let's say Starcraft2 and WoW are the best Blizzard games";
        String message4 = "What's about Diablo 3 ?";
        String message5 = "You're right, completely forgot it!";
        final UUID messageId1 = UUIDs.timeBased();
        final UUID messageId2 = UUIDs.timeBased();
        final UUID messageId3 = UUIDs.timeBased();
        final UUID messageId4 = UUIDs.timeBased();
        final UUID messageId5 = UUIDs.timeBased();

        Insert createMessage = insertInto(KEYSPACE, CHATROOM_MESSAGES)
                .value("room_name", bindMarker("room_name"))
                .value("message_id", bindMarker("message_id"))
                .value("author", bindMarker("author"))
                .value("content", bindMarker("content"))
                .value("system_message", bindMarker("system_message"));

        final PreparedStatement preparedStatement = session.prepare(createMessage);

        session.execute(preparedStatement.bind(roomName, messageId1, manager.serializeToJSON(johnDoe), message1, false));
        session.execute(preparedStatement.bind(roomName, messageId2, manager.serializeToJSON(helenSue), message2, false));
        session.execute(preparedStatement.bind(roomName, messageId3, manager.serializeToJSON(johnDoe), message3, false));
        session.execute(preparedStatement.bind(roomName, messageId4, manager.serializeToJSON(helenSue), message4, false));
        session.execute(preparedStatement.bind(roomName, messageId5, manager.serializeToJSON(johnDoe), message5, false));

        //When
        final List<ChatMessageModel> messages = service.fetchNextMessagesForRoom(roomName, UUIDs.timeBased(), 2);

        //Then
        assertThat(messages).hasSize(2);
        final ChatMessageModel lastMessage = messages.get(1);

        assertThat(lastMessage.getAuthor()).isEqualTo(johnDoe);
        assertThat(lastMessage.getMessageId()).isEqualTo(messageId5);
        assertThat(lastMessage.getContent()).isEqualTo(message5);

        final ChatMessageModel beforeLastMessage = messages.get(0);

        assertThat(beforeLastMessage.getAuthor()).isEqualTo(helenSue);
        assertThat(beforeLastMessage.getMessageId()).isEqualTo(messageId4);
        assertThat(beforeLastMessage.getContent()).isEqualTo(message4);
    }

    @Test
    public void should_fetch_some_message_starting_from_the_last_one_excluding() throws Exception {
        //Given
        String roomName = "games";
        String message1 = "Starcraft2 is awesome!";
        String message2 = "No, WoW is ways better";
        String message3 = "Ok, so let's say Starcraft2 and WoW are the best Blizzard games";
        String message4 = "What's about Diablo 3 ?";
        String message5 = "You're right, completely forgot it!";
        final UUID messageId1 = UUIDs.timeBased();
        final UUID messageId2 = UUIDs.timeBased();
        final UUID messageId3 = UUIDs.timeBased();
        final UUID messageId4 = UUIDs.timeBased();
        final UUID messageId5 = UUIDs.timeBased();

        Insert createMessage = insertInto(KEYSPACE, CHATROOM_MESSAGES)
                .value("room_name", bindMarker("room_name"))
                .value("message_id", bindMarker("message_id"))
                .value("author", bindMarker("author"))
                .value("content", bindMarker("content"))
                .value("system_message", bindMarker("system_message"));

        final PreparedStatement preparedStatement = session.prepare(createMessage);

        session.execute(preparedStatement.bind(roomName, messageId1, manager.serializeToJSON(johnDoe), message1, false));
        session.execute(preparedStatement.bind(roomName, messageId2, manager.serializeToJSON(helenSue), message2, false));
        session.execute(preparedStatement.bind(roomName, messageId3, manager.serializeToJSON(johnDoe), message3, false));
        session.execute(preparedStatement.bind(roomName, messageId4, manager.serializeToJSON(helenSue), message4, false));
        session.execute(preparedStatement.bind(roomName, messageId5, manager.serializeToJSON(johnDoe), message5, false));

        //When
        final List<ChatMessageModel> messages = service.fetchNextMessagesForRoom(roomName, messageId4, 2);

        //Then
        assertThat(messages).hasSize(2);
        final ChatMessageModel lastMessage = messages.get(1);

        assertThat(lastMessage.getAuthor()).isEqualTo(johnDoe);
        assertThat(lastMessage.getMessageId()).isEqualTo(messageId3);
        assertThat(lastMessage.getContent()).isEqualTo(message3);

        final ChatMessageModel beforeLastMessage = messages.get(0);

        assertThat(beforeLastMessage.getAuthor()).isEqualTo(helenSue);
        assertThat(beforeLastMessage.getMessageId()).isEqualTo(messageId2);
        assertThat(beforeLastMessage.getContent()).isEqualTo(message2);
    }
}