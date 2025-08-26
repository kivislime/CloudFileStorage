package com.kivislime.filestorage.exception;

import java.io.IOException;

public class FileUploadException extends RuntimeException {
    public FileUploadException(String s, IOException e) {
        super(s, e);
    }
}
