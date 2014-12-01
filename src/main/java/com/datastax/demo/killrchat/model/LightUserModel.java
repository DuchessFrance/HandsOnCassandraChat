package com.datastax.demo.killrchat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LightUserModel {

    @NotEmpty
    protected String login;

    @Size(max = 100)
    protected String firstname;

    @Size(max = 100)
    protected String lastname;

    @Size(max = 100)
    protected String nickname;

}
