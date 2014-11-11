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
public class UserModel {

    @NotEmpty
    private String login;

    @NotEmpty
    private String password;

    @Size(max = 100)
    private String firstname;

    @Size(max = 100)
    private String lastname;

    @Size(max = 100)
    private String nickname;

    @Email
    private String email;

    @Size(max = 2000)
    private String bio;
}
