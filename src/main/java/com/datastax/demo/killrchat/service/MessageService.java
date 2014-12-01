package com.datastax.demo.killrchat.service;

import info.archinnov.achilles.persistence.PersistenceManager;

import javax.inject.Inject;
import java.util.UUID;

public class MessageService {

    @Inject
    PersistenceManager manager;

    public void postNewMessage(String login, String messageContent) {

    }

    public void deleteMessage(String login, UUID messageId) {

    }
}
