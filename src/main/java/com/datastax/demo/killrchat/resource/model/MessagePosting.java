package com.datastax.demo.killrchat.resource.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessagePosting {

    @NotBlank
    private String author;

    @NotBlank
    private String content;

}
