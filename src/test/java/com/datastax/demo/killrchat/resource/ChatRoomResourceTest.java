package com.datastax.demo.killrchat.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.resource.model.ChatRoomCreationModel;
import com.datastax.demo.killrchat.resource.model.ChatRoomParticipantModel;
import com.datastax.demo.killrchat.service.ChatRoomService;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ChatRoomResourceTest {

    @InjectMocks
    private ChatRoomResource resource;

    @Mock
    private ChatRoomService service;

    @Test
    public void should_create_chat_room() throws Exception {
        //Given
        String roomName = "games";
        final LightUserModel userModel = new LightUserModel("jdoe", "John", "DOE", "johnny");

        //When
        resource.createChatRoom(new ChatRoomCreationModel(roomName, userModel, false, false));

        //Then
        verify(service).createChatRoom(roomName, userModel, false, false);
    }

    @Test
    public void should_find_room_by_name() throws Exception {
        //Given
        final ChatRoomModel roomModel = new ChatRoomModel("games","jdoe", "", false, false, Sets.<LightUserModel>newHashSet());
        when(service.findRoomByName("games")).thenReturn(roomModel);

        //When
        final ChatRoomModel found = resource.findRoomByName("games");

        //Then
        assertThat(found).isSameAs(roomModel);
    }

    @Test
    public void should_list_chat_rooms_from_the_beginning() throws Exception {
        //Given

        //When
        resource.listChatRooms(null,0);

        //Then
        verify(service).listChatRooms(ChatRoomResource.EMPTY_SPACE, ChatRoomResource.DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE);
    }

    @Test
    public void should_list_chat_rooms_from_lower_bound_with_page_size() throws Exception {
        //Given

        //When
        resource.listChatRooms("games", 11);

        //Then
        verify(service).listChatRooms("games", 11);
    }

    @Test
    public void should_list_chat_room_for_user_from_beginning() throws Exception {
        //Given
        final LightChatRoomModel room = new LightChatRoomModel("games","jdoe");
        when(service.listChatRoomsForUserByPage("jdoe","", ChatRoomResource.DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE)).thenReturn(Arrays.asList(room));

        //When
        final List<LightChatRoomModel> foundRooms = resource.listChatRoomsForUserByPage("jdoe", null, 0);

        //Then
        assertThat(foundRooms).containsExactly(room);
    }

    @Test
    public void should_list_chat_room_for_user_from_lower_bound_with_paging() throws Exception {
        //Given
        final LightChatRoomModel room = new LightChatRoomModel("games","jdoe");
        when(service.listChatRoomsForUserByPage("jdoe","fun", 11)).thenReturn(Arrays.asList(room));

        //When
        final List<LightChatRoomModel> foundRooms = resource.listChatRoomsForUserByPage("jdoe", "fun", 11);

        //Then
        assertThat(foundRooms).containsExactly(room);
    }

    @Test
    public void should_add_user_to_chat_room() throws Exception {
        //Given
        final LightUserModel user = new LightUserModel("jdoe", "John", "DOE", "johnny");
        final LightChatRoomModel room = new LightChatRoomModel("games","jdoe");

        //When
        resource.addUserToChatRoom(new ChatRoomParticipantModel(room, user));

        //Then
        verify(service).addUserToRoom(room, user);
    }

    @Test
    public void should_remove_user_from_chat_room() throws Exception {
        //Given
        final LightUserModel user = new LightUserModel("jdoe", "John", "DOE", "johnny");
        final LightChatRoomModel room = new LightChatRoomModel("games","jdoe");

        //When
        resource.removeUserFromChatRoom(new ChatRoomParticipantModel(room, user));

        //Then
        verify(service).removeUserFromRoom(room, user);
    }
}