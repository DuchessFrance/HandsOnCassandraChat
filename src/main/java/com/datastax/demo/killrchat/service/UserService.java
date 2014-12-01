package com.datastax.demo.killrchat.service;


import javax.inject.Inject;

import com.datastax.demo.killrchat.entity.User;
import com.datastax.demo.killrchat.exceptions.IncorrectOldPasswordException;
import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.exceptions.WrongLoginPasswordException;
import com.datastax.demo.killrchat.model.UserModel;
import info.archinnov.achilles.exception.AchillesLightWeightTransactionException;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;

import static info.archinnov.achilles.type.Options.LWTCondition;
import static info.archinnov.achilles.type.OptionsBuilder.ifConditions;
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

    public void changeUserPassword(String login, String oldPassword, String newPassword) {
        User user = fetchExistingUserFromCassandra(login);
        user.setPass(newPassword);
        try {
            manager.update(user, ifConditions(new LWTCondition("pass", oldPassword)));
        } catch (AchillesLightWeightTransactionException ex) {
            throw new IncorrectOldPasswordException("The provided old password does not match");
        }
    }

    private User fetchExistingUserFromCassandra(String login) {
        final User user = manager.find(User.class, login);
        if (user == null) {
            throw new UserNotFoundException(format("Cannot find user with login {}", login));
        }
        return user;
    }
}
