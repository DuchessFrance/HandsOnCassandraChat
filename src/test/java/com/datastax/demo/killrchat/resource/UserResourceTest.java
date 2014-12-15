package com.datastax.demo.killrchat.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.datastax.demo.killrchat.entity.User;
import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.resource.model.UserPasswordModel;
import com.datastax.demo.killrchat.service.ChatRoomService;
import com.datastax.demo.killrchat.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class UserResourceTest {

    @InjectMocks
    private UserResource resource;

    @Mock
    private UserService service;

    @Mock
    private ChatRoomService chatRoomService;

    @Test
    public void should_create_user() throws Exception {
        //Given
        final UserModel userModel = new UserModel("jdoe", "pass", "John", "DOE", "johnny", "jdoe@gmail.com", "bio");

        //When
        resource.createUser(userModel);

        //Then
        verify(service).createUser(userModel);
    }

    @Test
    public void should_find_user_by_login() throws Exception {
        //Given
        final User user = new User("jdoe", "pass", "John", "DOE", "johnny", "jdoe@gmail.com", "bio");
        when(service.findByLogin("jdoe")).thenReturn(user);

        //When
        final UserModel found = resource.findByLogin("jdoe");

        //Then
        assertThat(found).isEqualTo(user.toModel());
    }

    @Test
    public void should_list_users_from_login() throws Exception {
        //Given
        final UserModel userModel = new UserModel("jdoe", "pass", "John", "DOE", "johnny", "jdoe@gmail.com", "bio");

        when(service.listUsers("a", 12)).thenReturn(Arrays.asList(userModel));

        //When
        final List<UserModel> found = resource.listUsers("a", 12);

        //Then
        assertThat(found).containsExactly(userModel);
    }

    @Test
    public void should_change_user_password() throws Exception {
        //When
        final UserPasswordModel userPasswordModel = new UserPasswordModel("jdoe", "pass","new_pass");

        resource.changeUserPassword(userPasswordModel);

        //Then
        verify(service).changeUserPassword("jdoe", "pass", "new_pass");
    }

    @Test
    public void should_list_chat_room_for_user_from_beginning() throws Exception {
        //Given
        final LightChatRoomModel room = new LightChatRoomModel("games","jdoe");
        when(chatRoomService.listChatRoomsForUserByPage(null,"", ChatRoomResource.DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE)).thenReturn(Arrays.asList(room));

        //When
        final List<LightChatRoomModel> foundRooms = resource.listChatRoomsForUserByPage(null, 0);

        //Then
        assertThat(foundRooms).containsExactly(room);
    }

    @Test
    public void should_list_chat_room_for_user_from_lower_bound_with_paging() throws Exception {
        //Given
        final LightChatRoomModel room = new LightChatRoomModel("games","jdoe");
        when(chatRoomService.listChatRoomsForUserByPage(null,"fun", 11)).thenReturn(Arrays.asList(room));

        //When
        final List<LightChatRoomModel> foundRooms = resource.listChatRoomsForUserByPage("fun", 11);

        //Then
        assertThat(foundRooms).containsExactly(room);
    }
}