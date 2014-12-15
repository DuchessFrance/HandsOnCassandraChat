package com.datastax.demo.killrchat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LightUserModel {

    @NotEmpty
    protected String login;

    @Size(max = 100)
    protected String firstname;

    @Size(max = 100)
    protected String lastname;


}
