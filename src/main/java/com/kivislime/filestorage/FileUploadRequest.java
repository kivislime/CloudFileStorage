package com.kivislime.filestorage;

public record FileUploadRequest(String ordinalName, String contentType ,long size, byte[] bytes) {
}
