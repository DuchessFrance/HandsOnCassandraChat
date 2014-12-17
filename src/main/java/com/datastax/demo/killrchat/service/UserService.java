package com.datastax.demo.killrchat.service;


import javax.inject.Inject;

import com.datastax.demo.killrchat.entity.User;
import com.datastax.demo.killrchat.exceptions.IncorrectOldPasswordException;
import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Function;
import info.archinnov.achilles.exception.AchillesLightWeightTransactionException;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.datastax.demo.killrchat.entity.Schema.*;
import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.google.common.collect.FluentIterable.from;
import static info.archinnov.achilles.type.OptionsBuilder.ifEqualCondition;
import static java.lang.String.format;

@Service
public class UserService {

    @Inject
    PersistenceManager manager;

    public void createUser(UserModel model) {
        try {
            manager.insert(User.fromModel(model), OptionsBuilder.ifNotExists());
        } catch (AchillesLightWeightTransactionException ex) {
            throw new UserAlreadyExistsException(format("The user with the login '%s' already exists", model.getLogin()));
        }
    }

    public User findByLogin(String login) {
        final User user = manager.find(User.class, login);
        if (user == null) {
            throw new UserNotFoundException(format("Cannot find user with login '%s'", login));
        }
        return user;
    }
}
