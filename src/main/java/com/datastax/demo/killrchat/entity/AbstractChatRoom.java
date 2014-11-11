package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.UserModel;
import info.archinnov.achilles.annotations.Column;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;


@Data
public abstract class AbstractChatRoom {

    @NotBlank
    @Column
    protected String roomName;

    @NotEmpty
    @Column
    protected List<UserModel> participants;

    public abstract UUID getRoomId();

    public abstract ChatTheme getTheme();


}
