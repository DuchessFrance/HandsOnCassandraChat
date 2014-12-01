package com.datastax.demo.killrchat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LightChatRoomModel {

    @NotBlank
    protected String roomName;

    @NotBlank
    private String creator;
}
