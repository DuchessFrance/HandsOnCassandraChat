package com.datastax.demo.killrchat.resource.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordModel {

    @NotEmpty
    private String login;

    @NotEmpty
    private String password;

    @JsonProperty("new-password")
    private String newPassword;

    public UserPasswordModel(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
