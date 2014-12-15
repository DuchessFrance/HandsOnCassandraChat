CREATE KEYSPACE IF NOT EXISTS killrchat WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE IF NOT EXISTS killrchat.security_tokens(
    series text,
    token_date timestamp,
    user_agent text,
    token_value text,
    user text,
    ip_address text,
    PRIMARY KEY(series)
);

CREATE TABLE IF NOT EXISTS killrchat.chat_room_messages(
    room_name text,
    message_id timeuuid,
    content text,
    author text,
    system_message boolean,
    PRIMARY KEY(room_name, message_id)
);

CREATE TABLE IF NOT EXISTS killrchat.chat_rooms(
    room_name text,
    participants set<text>,
    creator text,
    PRIMARY KEY(room_name)
);

CREATE TABLE IF NOT EXISTS killrchat.user_chat_rooms(
    login text,
    room_name text,
    creator text,
    PRIMARY KEY(login, room_name)
);

CREATE TABLE IF NOT EXISTS killrchat.users(
    login text,
    bio text,
    email text,
    nickname text,
    lastname text,
    firstname text,
    pass text,
    PRIMARY KEY(login)
);
