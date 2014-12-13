package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.security.authority.CustomUserDetails;
import com.datastax.demo.killrchat.security.authority.UserAuthority;
import com.google.common.collect.Sets;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.USERS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(keyspace = KEYSPACE, table = USERS, comment = "users table")
public class User {

    @Id
    @NotEmpty
    private String login;

    @NotEmpty
    @Column
    private String pass;

    @Column
    private String firstname;

    @Column
    private String lastname;

    @Column
    private String nickname;

    @Column
    private String email;

    @Column
    private String bio;

    public static User fromModel(UserModel model) {
        final User user = new User();
        user.setLogin(model.getLogin());
        user.setPass(model.getPassword());
        user.setFirstname(model.getFirstname());
        user.setLastname(model.getLastname());
        user.setNickname(model.getNickname());
        user.setEmail(model.getEmail());
        user.setBio(model.getBio());
        return user;
    }

    public UserModel toModel() {
        final UserModel model = new UserModel();
        model.setLogin(this.getLogin());
        model.setPassword(this.getPass());
        model.setFirstname(this.getFirstname());
        model.setLastname(this.getLastname());
        model.setNickname(this.getNickname());
        model.setEmail(this.getEmail());
        model.setBio(this.getBio());
        return model;
    }

    public LightUserModel toLightModel() {
        final LightUserModel model = new LightUserModel();
        model.setLogin(this.getLogin());
        model.setFirstname(this.getFirstname());
        model.setLastname(this.getLastname());
        model.setNickname(this.getNickname());
        return model;
    }

    public CustomUserDetails toUserDetails() {
        return new CustomUserDetails(Sets.newHashSet(new UserAuthority()),this.login,this.pass);
    }
}
