package com.datastax.demo.killrchat.model;

import com.datastax.demo.killrchat.json.JsonDateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageModel {

    private UUID messageId;

    @NotNull
    @JsonSerialize(using = JsonDateSerializer.class)
    private Date creationDate;

    @NotNull
    private LightUserModel author;

    @NotBlank
    private String content;

    private boolean systemMessage;
}
