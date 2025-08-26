package com.kivislime.filestorage.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class FileAlreadyExistsException extends RuntimeException {
    public FileAlreadyExistsException(String s) {
        super(s);
    }

    public FileAlreadyExistsException(String s, Throwable e) {
        super(s, e);
    }
}
