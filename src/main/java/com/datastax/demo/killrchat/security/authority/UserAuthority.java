package com.datastax.demo.killrchat.security.authority;

import org.springframework.security.core.GrantedAuthority;

public class UserAuthority implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return AuthoritiesConstants.USER;
    }
}
