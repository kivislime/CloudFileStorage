package com.kivislime.filestorage.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class FileAlreadyExistsException extends RuntimeException {
    public FileAlreadyExistsException(String fileAlreadyExists, DataIntegrityViolationException e) {
    }
}
