package com.kivislime.filestorage.exception;

public class FileAlreadyExistsException extends RuntimeException {
    public FileAlreadyExistsException(String s) {
        super(s);
    }

    public FileAlreadyExistsException(String s, Throwable e) {
        super(s, e);
    }
}
