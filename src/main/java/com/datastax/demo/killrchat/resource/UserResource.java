package com.datastax.demo.killrchat.resource;

import com.datastax.demo.killrchat.exceptions.IncorrectOldPasswordException;
import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.exceptions.WrongLoginPasswordException;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.resource.model.UserPasswordModel;
import com.datastax.demo.killrchat.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/users")
public class UserResource {

    public static final int DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE = 10;

    public static final String EMPTY_SPACE = "";

    @Inject
    private UserService service;

    @RequestMapping(value = "/", method = POST, consumes = APPLICATION_JSON_VALUE)
    public void createUser(@NotNull @RequestBody UserModel model) {
        //TODO encrypt password properly for security
        service.createUser(model);
    }

    @RequestMapping(value = "/", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<UserModel> listUsers(String fromUserLogin, int fetchSize) {
        final String fromLogin = fromUserLogin == null ? EMPTY_SPACE : fromUserLogin;
        final int pageSize = fetchSize <= 0 ? DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE : fetchSize;
        return service.listUsers(fromLogin, pageSize);
    }

    @RequestMapping(value = "/login", method = POST, consumes = APPLICATION_JSON_VALUE)
    public void login(@NotNull @RequestBody UserPasswordModel userPassword) {
        final String login = userPassword.getLogin();
        final String password = userPassword.getPassword();
        Validator.validateNotBlank(login, "Missing login for user authentication");
        Validator.validateNotBlank(password, "Missing password for user authentication");

        //TODO hash password and check with the saved hash in Cassandra
        service.validatePasswordForUser(login, password);
    }

    @RequestMapping(value = "/password", method = PUT, consumes = APPLICATION_JSON_VALUE)
    public void changeUserPassword(@NotNull @RequestBody UserPasswordModel userPassword) {

        final String login = userPassword.getLogin();
        final String oldPassword = userPassword.getPassword();
        final String newPassword = userPassword.getNewPassword();

        Validator.validateNotBlank(login, "Missing login for user authentication");
        Validator.validateNotBlank(oldPassword, "Missing oldPassword for user authentication");
        Validator.validateNotBlank(newPassword, "Missing newPassword for user authentication");

        //TODO hash passwords properly for security
        service.changeUserPassword(login, oldPassword, newPassword);
    }


    @ExceptionHandler(value = {
            UserAlreadyExistsException.class,
            UserNotFoundException.class,
            WrongLoginPasswordException.class,
            IncorrectOldPasswordException.class
    })
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String userResourceException(Exception exception) {
        return exception.getMessage();
    }
}
