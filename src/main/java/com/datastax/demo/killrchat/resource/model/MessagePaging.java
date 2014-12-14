package com.datastax.demo.killrchat.resource.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessagePaging {

    @JsonProperty("from-message-id")
    private UUID fromMessageId;

    @JsonProperty("fetch-size")
    private int fetchSize;
}
