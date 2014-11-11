package com.datastax.demo.killrchat.exceptions;

public class WrongLoginPasswordException extends RuntimeException{
    public WrongLoginPasswordException(String message) {
        super(message);
    }
}
