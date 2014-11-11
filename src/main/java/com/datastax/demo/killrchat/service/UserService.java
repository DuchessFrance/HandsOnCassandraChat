package com.datastax.demo.killrchat.service;


import javax.inject.Inject;

import com.datastax.demo.killrchat.entity.User;
import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.exceptions.WrongLoginPasswordException;
import com.datastax.demo.killrchat.model.UserModel;
import info.archinnov.achilles.exception.AchillesLightWeightTransactionException;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;

import static java.lang.String.format;

public class UserService {

    @Inject
    PersistenceManager manager;

    public void createUser(UserModel model) {
        try {
            manager.insert(User.fromModel(model), OptionsBuilder.ifNotExists());
        } catch (AchillesLightWeightTransactionException ex) {
            throw new UserAlreadyExistsException(format("The user with the login {} already exists", model.getLogin()));
        }

    }

    public UserModel findByLogin(String login) {
        final User user = fetchExistingUserFromCassandra(login);
        return user.toModel();
    }

    public void validatePasswordForUser(String login, String password) {
        final User user = manager.find(User.class, login);
        if (!user.getPass().equals(password)) {
            throw new WrongLoginPasswordException("The login or password is incorrect");
        }
    }

    public void updateUser(UserModel userModel) {
        final User user = fetchExistingUserFromCassandra(userModel.getLogin());
        user.setFirstname(userModel.getFirstname());
        user.setLastname(userModel.getLastname());
        user.setNickname(userModel.getNickname());
        user.setBio((userModel.getBio()));
        user.setEmail(userModel.getEmail());
        manager.update(user);
    }

    private User fetchExistingUserFromCassandra(String login) {
        final User user = manager.find(User.class, login);
        if (user == null) {
            throw new UserNotFoundException(format("Cannot find user with login {}", login));
        }
        return user;
    }
}
