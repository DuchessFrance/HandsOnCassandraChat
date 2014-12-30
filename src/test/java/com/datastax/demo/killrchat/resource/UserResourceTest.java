package com.datastax.demo.killrchat.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.datastax.demo.killrchat.entity.UserEntity;
import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.model.UserModel;
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

    private LightUserModel john = new LightUserModel("jdoe", "John", "DOE");

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
        final UserEntity userEntity = new UserEntity("jdoe", "pass", "John", "DOE", "johnny", "jdoe@gmail.com", "bio");
        when(service.findByLogin("jdoe")).thenReturn(userEntity);

        //When
        final UserModel found = resource.findByLogin("jdoe");

        //Then
        assertThat(found).isEqualTo(userEntity.toModel());
    }
}