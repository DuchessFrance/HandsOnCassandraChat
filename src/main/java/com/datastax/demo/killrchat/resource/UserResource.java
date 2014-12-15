package com.datastax.demo.killrchat.resource;

import com.datastax.demo.killrchat.exceptions.IncorrectOldPasswordException;
import com.datastax.demo.killrchat.exceptions.UserAlreadyExistsException;
import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.model.LightChatRoomModel;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.resource.model.UserPasswordModel;
import com.datastax.demo.killrchat.security.utils.SecurityUtils;
import com.datastax.demo.killrchat.service.ChatRoomService;
import com.datastax.demo.killrchat.service.UserService;
import org.apache.commons.lang3.StringUtils;
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

    @Inject
    private ChatRoomService chatRoomService;

    @RequestMapping(value = "/create", method = POST, consumes = APPLICATION_JSON_VALUE)
    public void createUser(@NotNull @RequestBody UserModel model) {
        //TODO encrypt password properly for security
        service.createUser(model);
    }

    @RequestMapping(value = "/{login}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public UserModel findByLogin(@PathVariable String login) {
        return service.findByLogin(login).toModel();
    }

    @RequestMapping(value = "/list", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<UserModel> listUsers(@RequestParam(required = false) String fromUserLogin, @RequestParam(required = false) int fetchSize) {
        final int pageSize = fetchSize <= 0 ? DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE : fetchSize;
        return service.listUsers(fromUserLogin, pageSize);
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

    @RequestMapping(value = "/rooms", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<LightChatRoomModel> listChatRoomsForUserByPage(@RequestParam(required = false) String fromRoomName, @RequestParam(required = false) int fetchSize) {
        final String login = SecurityUtils.getCurrentLogin();
        final String fromRoom = StringUtils.isBlank(fromRoomName) ? EMPTY_SPACE : fromRoomName;
        final int pageSize = fetchSize <= 0 ? DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE :fetchSize;
        return chatRoomService.listChatRoomsForUserByPage(login, fromRoom, pageSize);
    }

    @ExceptionHandler(value = {
            UserAlreadyExistsException.class,
            UserNotFoundException.class,
            IncorrectOldPasswordException.class
    })
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String userResourceException(Exception exception) {
        return exception.getMessage();
    }
}
