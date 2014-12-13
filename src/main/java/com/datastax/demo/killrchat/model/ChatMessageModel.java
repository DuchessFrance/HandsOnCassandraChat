package com.datastax.demo.killrchat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageModel {

    @JsonProperty("message-id")
    private UUID messageId;

    @JsonProperty("creation-date")
    @NotNull
    private Date creationDate;


    @NotBlank
    private String author;

    @NotBlank
    private String content;

    @JsonProperty("system-message")
    private boolean systemMessage;
}
