package com.datastax.demo.killrchat.entity;

public interface Schema {
    static String KEYSPACE = "killrchat";
    static String USERS = "users";
    static String CHATROOMS_BY_THEME = "chat_rooms_by_theme";
    static String USER_CHATROOMS = "user_chat_room";
}
