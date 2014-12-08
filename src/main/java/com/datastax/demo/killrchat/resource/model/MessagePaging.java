package com.datastax.demo.killrchat.resource.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessagePaging {

    private UUID fromMessageId;

    private int fetchSize;
}
