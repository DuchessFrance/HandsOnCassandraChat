package com.datastax.demo.killrchat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private Date creationDate;


    @NotBlank
    private String author;

    @NotBlank
    private String content;

    private boolean systemMessage;
}
