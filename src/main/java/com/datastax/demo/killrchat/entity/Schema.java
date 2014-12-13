package com.datastax.demo.killrchat.entity;

public interface Schema {
    static String KEYSPACE = "killrchat";
    static String USERS = "users";
    static String CHATROOMS = "chat_rooms";
    static String USER_CHATROOMS = "user_chat_rooms";
    static String CHATROOM_MESSAGES = "chat_room_messages";
    static String PERSISTENT_TOKEN = "security_tokens";
}
