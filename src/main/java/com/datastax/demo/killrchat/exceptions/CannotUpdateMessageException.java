package com.datastax.demo.killrchat.exceptions;

public class CannotUpdateMessageException extends RuntimeException{
    public CannotUpdateMessageException(String message) {
        super(message);
    }
}
