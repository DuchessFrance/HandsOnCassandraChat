package com.datastax.demo.killrchat.resource;

import com.datastax.demo.killrchat.exceptions.IncorrectOldPasswordException;
import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.exceptions.WrongLoginPasswordException;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.service.UserService;

import javax.inject.Inject;

public class UserResource {

    @Inject
    private UserService service;

    public void createUser(UserModel model) {
        //TODO encrypt password properly for security
        try {
            service.createUser(model);
        } catch (UserAlreadyExistsException ex) {

        }
    }

    public void login(String login, String password) {
        Validator.validateNotBlank(login, "Missing login for user authentication");
        Validator.validateNotBlank(password, "Missing password for user authentication");

        //TODO hash password and check with the saved hash in Cassandra
        try {
            service.validatePasswordForUser(login, password);
        } catch (UserNotFoundException ex) {

        } catch (WrongLoginPasswordException ex) {

        }
    }

    public void changeUserPassword(String login, String oldPassword, String newPassword) {
        Validator.validateNotBlank(login, "Missing login for user authentication");
        Validator.validateNotBlank(oldPassword, "Missing oldPassword for user authentication");
        Validator.validateNotBlank(newPassword, "Missing newPassword for user authentication");

        //TODO hash passwords properly for security
        try {
            service.changeUserPassword(login, oldPassword, newPassword);
        } catch (UserNotFoundException ex) {

        } catch (IncorrectOldPasswordException ex) {

        }
    }
}
