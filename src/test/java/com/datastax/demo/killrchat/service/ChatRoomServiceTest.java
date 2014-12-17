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

    public static final String EMPTY_SPACE = "";

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

    private LightUserModel john = new LightUserModel("jdoe", "John", "DOE");

    private LightUserModel helen = new LightUserModel("hsue", "Helen", "SUE");

    @Before
    public void setUp() {
        service.manager = this.manager;
    }

    @Test
    public void should_create_chat_room() throws Exception {
        //Given
        final String roomName = "random_thoughts";

        //When
        service.createChatRoom(roomName, john);

        //Then
        final Row chatRoomByTheme = session.execute(select().from(KEYSPACE, CHATROOMS).where(eq("room_name", "random_thoughts"))).one();
        final List<Row> jdoeChatRooms = session.execute(select().from(KEYSPACE, USER_CHATROOMS).where(eq("login", "jdoe")).limit(10)).all();

        assertThat(chatRoomByTheme).isNotNull();
        assertThat(chatRoomByTheme.getString("room_name")).isEqualTo(roomName);
        assertThat(chatRoomByTheme.getSet("participants", String.class)).contains(manager.serializeToJSON(john));

        assertThat(jdoeChatRooms).hasSize(1);
        assertThat(jdoeChatRooms.get(0).getString("room_name")).isEqualTo(roomName);
    }

    @Test(expected = ChatRoomAlreadyExistsException.class)
    public void should_exception_when_creating_existing_chat_room() throws Exception {
        //Given
        session.execute(insertInto(KEYSPACE, CHATROOMS).value("room_name", "all"));

        //When
        service.createChatRoom("all", new LightUserModel("jdoe","John", "DOE"));
    }

    @Test
    public void should_find_room_by_name() throws Exception {
        //Given
        final Insert insertRoom = insertInto(KEYSPACE, CHATROOMS)
                .value("room_name", "games")
                .value("creator", manager.serializeToJSON(john))
                .value("participants", Sets.<LightUserModel>newHashSet());

        session.execute(insertRoom);

        //When
        final ChatRoomModel model = service.findRoomByName("games");

        //Then
        assertThat(model.getCreator()).isEqualTo(john);
        assertThat(model.getRoomName()).isEqualTo("games");
        assertThat(model.getParticipants()).isNull();
    }

    @Test(expected = ChatRoomDoesNotExistException.class)
    public void should_exception_when_room_does_not_exist() throws Exception {
        service.findRoomByName("games");
    }

    @Test
    public void should_list_chat_rooms_by_page() throws Exception {
        //Given
        final String johnAsJSON = manager.serializeToJSON(john);

        final Insert starcraftRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "starcraft").value("creator", johnAsJSON);
        final Insert callOfDutyRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "call_of_duty").value("creator", johnAsJSON);
        final Insert bioshockRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "bioshock").value("creator", johnAsJSON);
        final Insert javaRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "java").value("creator", johnAsJSON);
        final Insert scalaRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "scala").value("creator", johnAsJSON);
        final Insert saasRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "saas").value("creator", johnAsJSON);
        final Insert jenniferLaurenceRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "team_jennifer_laurence").value("creator", johnAsJSON);
        final Insert politicsRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "politics").value("creator", johnAsJSON);

        final Batch batch = batch(starcraftRoom, callOfDutyRoom, bioshockRoom, javaRoom, scalaRoom, saasRoom, jenniferLaurenceRoom, politicsRoom);
        session.execute(batch);

        //When
        final List<ChatRoomModel> rooms = service.listChatRooms("bioshock", 3);

        //Then
        assertThat(rooms).hasSize(3);
        assertThat(rooms.get(0).getRoomName()).isEqualTo("java");
        assertThat(rooms.get(1).getRoomName()).isEqualTo("scala");
        assertThat(rooms.get(2).getRoomName()).isEqualTo("politics");
    }

    @Test
    public void should_list_first_page_of_rooms() throws Exception {
        //Given
        final String johnAsJSON = manager.serializeToJSON(john);
        final Insert starcraftRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "starcraft").value("creator", johnAsJSON);
        final Insert callOfDutyRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "call_of_duty").value("creator", johnAsJSON);
        final Insert bioshockRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "bioshock").value("creator", johnAsJSON);
        final Insert javaRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "java").value("creator", johnAsJSON);
        final Insert scalaRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "scala").value("creator", johnAsJSON);
        final Insert saasRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "saas").value("creator", johnAsJSON);
        final Insert jenniferLaurenceRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "team_jennifer_laurence").value("creator", johnAsJSON);
        final Insert politicsRoom = insertInto(KEYSPACE, CHATROOMS).value("room_name", "politics").value("creator", johnAsJSON);

        final Batch batch = batch(starcraftRoom, callOfDutyRoom, bioshockRoom, javaRoom, scalaRoom, saasRoom, jenniferLaurenceRoom, politicsRoom);
        session.execute(batch);

        //When
        final List<ChatRoomModel> rooms = service.listChatRooms(EMPTY_SPACE, 3);

        //Then
        assertThat(rooms).hasSize(3);
        assertThat(rooms.get(0).getRoomName()).isEqualTo("saas");
        assertThat(rooms.get(1).getRoomName()).isEqualTo("bioshock");
        assertThat(rooms.get(2).getRoomName()).isEqualTo("java");
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
        final LightChatRoomModel chatRoomModel = new LightChatRoomModel("politics", john);
        final String johnAsJSON = manager.serializeToJSON(john);
        final String helenAsJSON = manager.serializeToJSON(helen);

        final Insert insert = insertInto(KEYSPACE, CHATROOMS)
                .value("room_name", "politics")
                .value("creator", johnAsJSON)
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
        assertThat(userRoomRow.getString("creator")).isEqualTo(johnAsJSON);
    }

    @Test(expected = ChatRoomDoesNotExistException.class)
    public void should_exception_when_adding_user_to_non_existing_chat_room() throws Exception {
        //Given
        final LightChatRoomModel chatRoomModel = new LightChatRoomModel("politics", john);

        //When
        service.addUserToRoom(chatRoomModel, helen);
    }

    @Test
    public void should_remove_user_from_chat_room() throws Exception {
        //Given
        final LightChatRoomModel chatRoomModel = new LightChatRoomModel("politics", john);
        final String johnAsJSON = manager.serializeToJSON(john);
        final String helenAsJSON = manager.serializeToJSON(helen);

        final Insert createChatRoomStatement = insertInto(KEYSPACE, CHATROOMS)
                .value("room_name", "politics")
                .value("creator", johnAsJSON)
                .value("participants", Arrays.asList(johnAsJSON, helenAsJSON));

        final Insert createHelenChatRooms = insertInto(KEYSPACE, USER_CHATROOMS)
                .value("login", "hsue")
                .value("room_name", "politics")
                .value("creator", johnAsJSON);

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
        final LightChatRoomModel chatRoomModel = new LightChatRoomModel("politics", john);
        final LightUserModel helen = new LightUserModel("hsue", "Helen", "SUE");

        //When
        service.removeUserFromRoom(chatRoomModel, helen);
    }

    @Test
    public void should_remove_last_user_from_chat_room() throws Exception {
        //Given
        final LightChatRoomModel chatRoomModel = new LightChatRoomModel("politics", john);
        final String johnAsJSON = manager.serializeToJSON(john);

        final Insert createChatRoomStatement = insertInto(KEYSPACE, CHATROOMS)
                .value("room_name", "politics")
                .value("creator", johnAsJSON)
                .value("participants", Arrays.asList(johnAsJSON));

        final Insert createJohnChatRooms = insertInto(KEYSPACE, USER_CHATROOMS)
                .value("login", "hsue")
                .value("room_name", "politics")
                .value("creator", johnAsJSON);


        session.execute(createChatRoomStatement);
        session.execute(createJohnChatRooms);

        //When
        service.removeUserFromRoom(chatRoomModel, john);

        //Then
        final Select.Where participants = select().from(KEYSPACE, CHATROOMS).where(eq("room_name", "politics"));
        final Select johnChatRooms = select().from(KEYSPACE, USER_CHATROOMS).where(eq("login", "jdoe")).limit(10);

        final Row chatRoom = session.execute(participants).one();
        assertThat(chatRoom).isNull();

        final List<Row> johnChatRoomsRows = session.execute(johnChatRooms).all();
        assertThat(johnChatRoomsRows).hasSize(0);
    }
}