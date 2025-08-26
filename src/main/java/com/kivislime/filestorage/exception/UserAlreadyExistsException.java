package com.kivislime.filestorage.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String s, Throwable e) {
        super(s, e);
    }
}
