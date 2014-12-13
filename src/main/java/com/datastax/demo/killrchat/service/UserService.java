package com.datastax.demo.killrchat.service;


import javax.inject.Inject;

import com.datastax.demo.killrchat.entity.User;
import com.datastax.demo.killrchat.exceptions.IncorrectOldPasswordException;
import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.exceptions.WrongLoginPasswordException;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Function;
import info.archinnov.achilles.exception.AchillesLightWeightTransactionException;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.USERS;
import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.google.common.collect.FluentIterable.from;
import static info.archinnov.achilles.type.Options.LWTCondition;
import static info.archinnov.achilles.type.OptionsBuilder.ifConditions;
import static info.archinnov.achilles.type.OptionsBuilder.ifEqualCondition;
import static java.lang.String.format;

@Service
public class UserService {

    @Inject
    PersistenceManager manager;


    private static final Function<User, UserModel> USER_TO_MODEL = new Function<User, UserModel>() {
        @Override
        public UserModel apply(User entity) {
            return entity.toModel();
        }
    };

    public void createUser(UserModel model) {
        try {
            manager.insert(User.fromModel(model), OptionsBuilder.ifNotExists());
        } catch (AchillesLightWeightTransactionException ex) {
            throw new UserAlreadyExistsException(format("The user with the login {} already exists", model.getLogin()));
        }
    }

    public List<UserModel> listUsers(String fromUserLogin, int fetchSize) {
        final Select select = select().from(KEYSPACE, USERS)
                .where(gt(token("login"), fcall("token",fromUserLogin)))
                .limit(fetchSize);

        final List<User> foundUsers = manager.typedQuery(User.class, select).get();
        return from(foundUsers).transform(USER_TO_MODEL).toList();
    }

    public void validatePasswordForUser(String login, String password) {
        final User user = findByLogin(login);
        if (!user.getPass().equals(password)) {
            throw new WrongLoginPasswordException("The login or password is incorrect");
        }
    }

    public void changeUserPassword(String login, String oldPassword, String newPassword) {
        User user = findByLogin(login);
        user.setPass(newPassword);
        try {
            manager.update(user, ifEqualCondition("pass", oldPassword));
        } catch (AchillesLightWeightTransactionException ex) {
            throw new IncorrectOldPasswordException("The provided old password does not match");
        }
    }

    public User findByLogin(String login) {
        final User user = manager.find(User.class, login);
        if (user == null) {
            throw new UserNotFoundException(format("Cannot find user with login {}", login));
        }
        return user;
    }
}
