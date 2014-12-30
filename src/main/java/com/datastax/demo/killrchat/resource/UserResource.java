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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping("/users")
public class UserResource {

    public static final int DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE = 100;

    public static final String EMPTY_SPACE = "";

    @Inject
    private UserService service;

    @Inject
    private ChatRoomService chatRoomService;

    @RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createUser(@NotNull @RequestBody @Valid UserModel model) {
        //TODO encrypt password properly for security
        service.createUser(model);
    }

    @RequestMapping(value = "/{login:.+}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public UserModel findByLogin(@PathVariable String login) {
        return service.findByLogin(login).toModel();
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
