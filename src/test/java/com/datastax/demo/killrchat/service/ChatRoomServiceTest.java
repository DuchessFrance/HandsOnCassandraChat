package com.datastax.demo.killrchat.service;

import static com.datastax.demo.killrchat.entity.Schema.*;
import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static org.assertj.core.api.Assertions.*;

import com.datastax.demo.killrchat.entity.User;
import com.datastax.demo.killrchat.exceptions.ChatRoomAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.ChatRoomDoesNotExistException;
import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.resource.ChatRoomResource;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Sets;
import info.archinnov.achilles.junit.AchillesResource;
import info.archinnov.achilles.junit.AchillesResourceBuilder;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ChatRoomServiceTest {

    @Rule
    public AchillesResource resource = AchillesResourceBuilder
            .withEntityPackages(User.class.getPackage().getName())
            .withKeyspaceName(KEYSPACE)
            .withBeanValidation()
            .tablesToTruncate(CHATROOMS,USER_CHATROOMS)
            .truncateBeforeAndAfterTest().build();

    private PersistenceManager manager = resource.getPersistenceManager();

    private Session session = resource.getNativeSession();

    private ChatRoomService service = new ChatRoomService();

    @Before
    public void setUp() {
        service.manager = this.manager;
    }

    @Test
    public void should_create_chat_room() throws Exception {
        //Given
        LightUserModel jdoe = new LightUserModel("jdoe","John", "DOE", "johnny");
        final String roomName = "random_thoughts";

        //When
        service.createChatRoom(roomName, jdoe, true, true);

        //Then
        final Row chatRoomByTheme = session.execute(select().from(KEYSPACE, CHATROOMS).where(eq("room_name", "random_thoughts"))).one();
        final List<Row> jdoeChatRooms = session.execute(select().from(KEYSPACE, USER_CHATROOMS).where(eq("login", "jdoe")).limit(10)).all();

        assertThat(chatRoomByTheme).isNotNull();
        assertThat(chatRoomByTheme.getString("room_name")).isEqualTo(roomName);
        assertThat(chatRoomByTheme.getSet("participants", String.class)).contains(manager.serializeToJSON(jdoe));

        assertThat(jdoeChatRooms).hasSize(1);
        assertThat(jdoeChatRooms.get(0).getString("room_name")).isEqualTo(roomName);
    }

    @Test(expected = ChatRoomAlreadyExistsException.class)
    public void should_exception_when_creating_existing_chat_room() throws Exception {
        //Given
        session.execute(insertInto(KEYSPACE, CHATROOMS).value("room_name", "all"));

        //When
        service.createChatRoom("all", new LightUserModel("jdoe","John", "DOE", "johnny"), true, true);
    }

    @Test
    public void should_find_room_by_name() throws Exception {
        //Given
        final Insert insertRoom = insertInto(KEYSPACE, CHATROOMS)
                .value("room_name", "games")
                .value("creator", "jdoe")
                .value("private_room", false)
                .value("direct_chat", false)
                .value("participants", Sets.<LightUserModel>newHashSet());


        session.execute(insertRoom);

        //When
        final ChatRoomModel model = service.findRoomByName("games");

        //Then
        assertThat(model.getCreator()).isEqualTo("jdoe");
        assertThat(model.getRoomName()).isEqualTo("games");
        assertThat(model.isDirectChat()).isFalse();
        assertThat(model.isPrivateRoom()).isFalse();
        assertThat(model.getParticipants()).isNull();
    }

    @Test(expected = ChatRoomDoesNotExistException.class)
    public void should_exception_when_room_does_not_exist() throws Exception {
        service.findRoomByName("games");
    }

    @Test
    public void should_list_chat_rooms_by_page() throws Exception {
        //Given
        service.roomFetchPage = 2;

        final List<String> allRooms = Arrays.asList("starcraft", "call_of_duty", "bioshock", "java", "scala", "saas", "team_jennifer_laurence", "politics");
        final Insert starcraftRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "starcraft").value("creator", "jdoe").value("private_room", false).value("direct_chat", false);
        final Insert callOfDutyRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "call_of_duty").value("creator", "jdoe").value("private_room", false).value("direct_chat", false);
        final Insert bioshockRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "bioshock").value("creator", "jdoe").value("private_room", false).value("direct_chat", false);
        final Insert javaRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "java").value("creator", "jdoe").value("private_room", false).value("direct_chat", false);
        final Insert scalaRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "scala").value("creator", "jdoe").value("private_room", false).value("direct_chat", false);
        final Insert saasRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "saas").value("creator", "jdoe").value("private_room", false).value("direct_chat", false);
        final Insert jenniferLaurenceRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "team_jennifer_laurence").value("creator", "jdoe").value("private_room", false).value("direct_chat", false);
        final Insert politicsRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "politics").value("creator", "jdoe").value("private_room", false).value("direct_chat", false);

        final Batch batch = batch(starcraftRoom, callOfDutyRoom, bioshockRoom, javaRoom, scalaRoom, saasRoom, jenniferLaurenceRoom, politicsRoom);
        session.execute(batch);

        //When
        final List<ChatRoomModel> rooms = service.listChatRooms(ChatRoomResource.EMPTY_SPACE, 3);

        //Then
        assertThat(rooms).hasSize(3);
        assertThat(allRooms).contains(rooms.get(0).getRoomName());
        assertThat(allRooms).contains(rooms.get(1).getRoomName());
        assertThat(allRooms).contains(rooms.get(2).getRoomName());
    }

    @Test
    public void should_find_rooms_by_user() throws Exception {
        //Given
        final Insert javaRoom = insertInto(KEYSPACE, USER_CHATROOMS).value("login", "jdoe").value("room_name", "java");
        final Insert starCraftRoom = insertInto(KEYSPACE, USER_CHATROOMS).value("login", "jdoe").value("room_name", "starcraft");

        session.execute(starCraftRoom);
        session.execute(javaRoom);

        //When
        final List<LightChatRoomModel> userRooms = service.listChatRoomsForUserByPage("jdoe", "", 100);

        //Then
        assertThat(userRooms).extracting("roomName").containsExactly("java","starcraft");
    }

    @Test
    public void should_find_rooms_by_user_by_page() throws Exception {
        //Given
        final Insert gossipRoom = insertInto(KEYSPACE, USER_CHATROOMS).value("login", "jdoe").value("room_name", "gossip");
        final Insert javaRoom = insertInto(KEYSPACE, USER_CHATROOMS).value("login", "jdoe").value("room_name", "java");
        final Insert politicsRoom = insertInto(KEYSPACE, USER_CHATROOMS).value("login", "jdoe").value("room_name", "politics");
        final Insert scalaRoom = insertInto(KEYSPACE, USER_CHATROOMS).value("login", "jdoe").value("room_name", "scala");
        final Insert starCraftRoom = insertInto(KEYSPACE, USER_CHATROOMS).value("login", "jdoe").value("room_name", "starcraft");

        final Batch batch = batch(gossipRoom, javaRoom, politicsRoom, scalaRoom, starCraftRoom);
        session.execute(batch);

        //When
        final List<LightChatRoomModel> userRooms = service.listChatRoomsForUserByPage("jdoe", "java", 2);

        //Then
        assertThat(userRooms).extracting("roomName").containsExactly("politics","scala");
    }

    @Test
    public void should_add_user_to_room() throws Exception {
        //Given
        final LightUserModel johnny = new LightUserModel("jdoe", "John", "DOE", "johnny");
        final LightUserModel helen = new LightUserModel("hsue", "Helen", "SUE", "helena");
        final LightChatRoomModel chatRoomModel = new LightChatRoomModel("politics", "jdoe");
        final String johnAsJSON = manager.serializeToJSON(johnny);
        final String helenAsJSON = manager.serializeToJSON(helen);

        final Insert insert = insertInto(KEYSPACE, CHATROOMS)
                .value("room_name", "politics")
                .value("creator", "jdoe")
                .value("private_room", false)
                .value("direct_chat", false)
                .value("participants", Arrays.asList(johnAsJSON));

        session.execute(insert);

        //When
        service.addUserToRoom(chatRoomModel, helen);

        //Then
        final Select.Where participants = select("participants").from(KEYSPACE, CHATROOMS).where(eq("room_name", "politics"));
        final Select helenChatRooms = select().from(KEYSPACE, USER_CHATROOMS).where(eq("login", "hsue")).limit(10);

        final Row participantsRow = session.execute(participants).one();

        assertThat(participantsRow.getSet("participants",String.class)).containsOnly(johnAsJSON, helenAsJSON);

        final List<Row> helenChatRoomsRows = session.execute(helenChatRooms).all();
        assertThat(helenChatRoomsRows).hasSize(1);
        final Row userRoomRow = helenChatRoomsRows.get(0);
        assertThat(userRoomRow.getString("room_name")).isEqualTo("politics");
        assertThat(userRoomRow.getString("creator")).isEqualTo("jdoe");
    }

    @Test(expected = ChatRoomDoesNotExistException.class)
    public void should_exception_when_adding_user_to_non_existing_chat_room() throws Exception {
        //Given
        final LightUserModel helen = new LightUserModel("hsue", "Helen", "SUE", "helena");
        final LightChatRoomModel chatRoomModel = new LightChatRoomModel("politics", "jdoe");

        //When
        service.addUserToRoom(chatRoomModel, helen);
    }

    @Test
    public void should_remove_user_from_chat_room() throws Exception {
        //Given
        final LightUserModel johnny = new LightUserModel("jdoe", "John", "DOE", "johnny");
        final LightUserModel helen = new LightUserModel("hsue", "Helen", "SUE", "helena");
        final LightChatRoomModel chatRoomModel = new LightChatRoomModel("politics", "jdoe");
        final String johnAsJSON = manager.serializeToJSON(johnny);
        final String helenAsJSON = manager.serializeToJSON(helen);

        final Insert createChatRoomStatement = insertInto(KEYSPACE, CHATROOMS)
                .value("room_name", "politics")
                .value("creator", "jdoe")
                .value("private_room", false)
                .value("direct_chat", false)
                .value("participants", Arrays.asList(johnAsJSON, helenAsJSON));

        final Insert createHelenChatRooms = insertInto(KEYSPACE, USER_CHATROOMS)
                .value("login", "hsue")
                .value("room_name", "politics")
                .value("creator", "jdoe");

        session.execute(createChatRoomStatement);
        session.execute(createHelenChatRooms);

        //When
        service.removeUserFromRoom(chatRoomModel, helen);

        //Then
        final Select.Where participants = select("participants").from(KEYSPACE, CHATROOMS).where(eq("room_name", "politics"));
        final Select helenChatRooms = select().from(KEYSPACE, USER_CHATROOMS).where(eq("login", "hsue")).limit(10);

        final Row participantsRow = session.execute(participants).one();
        assertThat(participantsRow).isNotNull();
        assertThat(participantsRow.getSet("participants", String.class)).containsOnly(johnAsJSON);

        final List<Row> helenChatRoomsRows = session.execute(helenChatRooms).all();
        assertThat(helenChatRoomsRows).hasSize(0);
    }
    
    @Test(expected = ChatRoomDoesNotExistException.class)
    public void should_exception_when_removing_user_from_non_existing_room() throws Exception {
        //Given
        final LightChatRoomModel chatRoomModel = new LightChatRoomModel("politics", "jdoe");
        final LightUserModel helen = new LightUserModel("hsue", "Helen", "SUE", "helena");

        //When
        service.removeUserFromRoom(chatRoomModel, helen);
    }

    @Test
    public void should_remove_last_user_from_chat_room() throws Exception {
        //Given
        final LightUserModel johnny = new LightUserModel("jdoe", "John", "DOE", "johnny");
        final LightChatRoomModel chatRoomModel = new LightChatRoomModel("politics", "jdoe");
        final String johnAsJSON = manager.serializeToJSON(johnny);

        final Insert createChatRoomStatement = insertInto(KEYSPACE, CHATROOMS)
                .value("room_name", "politics")
                .value("creator", "jdoe")
                .value("private_room", false)
                .value("direct_chat", false)
                .value("participants", Arrays.asList(johnAsJSON));

        final Insert createJohnChatRooms = insertInto(KEYSPACE, USER_CHATROOMS)
                .value("login", "hsue")
                .value("room_name", "politics")
                .value("creator", "jdoe");


        session.execute(createChatRoomStatement);
        session.execute(createJohnChatRooms);

        //When
        service.removeUserFromRoom(chatRoomModel, johnny);

        //Then
        final Select.Where participants = select().from(KEYSPACE, CHATROOMS).where(eq("room_name", "politics"));
        final Select johnChatRooms = select().from(KEYSPACE, USER_CHATROOMS).where(eq("login", "jdoe")).limit(10);

        final Row chatRoom = session.execute(participants).one();
        assertThat(chatRoom).isNull();

        final List<Row> johnChatRoomsRows = session.execute(johnChatRooms).all();
        assertThat(johnChatRoomsRows).hasSize(0);
    }
}