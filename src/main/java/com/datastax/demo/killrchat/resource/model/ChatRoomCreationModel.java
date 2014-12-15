package com.datastax.demo.killrchat.resource.model;

import com.datastax.demo.killrchat.model.LightUserModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoomCreationModel {

    @NotBlank
    private String roomName;

    @NotNull
    private LightUserModel creator;
}
