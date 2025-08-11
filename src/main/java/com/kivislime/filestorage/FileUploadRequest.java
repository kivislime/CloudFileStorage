package com.kivislime.filestorage;

import java.io.InputStream;

public record FileUploadRequest(String ordinalName, String contentType, long size, InputStream inputStream) {
}
