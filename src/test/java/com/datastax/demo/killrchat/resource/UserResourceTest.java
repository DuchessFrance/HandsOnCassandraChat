package com.datastax.demo.killrchat.resource;

import static org.mockito.Mockito.*;

import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserResourceTest {

    @InjectMocks
    private UserResource resource;

    @Mock
    private UserService service;

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
    public void should_login() throws Exception {
        //When
        resource.login("jdoe","pass");

        //Then
        verify(service).validatePasswordForUser("jdoe","pass");
    }

    @Test
    public void should_change_user_password() throws Exception {
        //When
        resource.changeUserPassword("jdoe","pass","new_pass");

        //Then
        verify(service).changeUserPassword("jdoe", "pass", "new_pass");
    }
}