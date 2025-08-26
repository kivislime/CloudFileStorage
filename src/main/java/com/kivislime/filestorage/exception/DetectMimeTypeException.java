package com.kivislime.filestorage.exception;

import java.io.IOException;

public class DetectMimeTypeException extends RuntimeException {
    public DetectMimeTypeException(String s, Throwable e) {
        super(s, e);
    }
}
