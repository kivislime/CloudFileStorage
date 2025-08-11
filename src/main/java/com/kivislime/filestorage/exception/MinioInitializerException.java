package com.kivislime.filestorage.exception;

public class MinioInitializerException extends RuntimeException {
    public MinioInitializerException(String cannotCreateBucket, Exception e) {
    }
}
