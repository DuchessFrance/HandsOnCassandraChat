package com.datastax.demo.killrchat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserModel extends LightUserModel {

    @NotEmpty
    private String password;

    @Email
    private String email;

    @Size(max = 2000)
    private String bio;

    public UserModel(String login, String password, String firstname, String lastname, String nickname, String email, String bio) {
        super(login, firstname, lastname, nickname);
        this.password = password;
        this.email = email;
        this.bio = bio;
    }
}
