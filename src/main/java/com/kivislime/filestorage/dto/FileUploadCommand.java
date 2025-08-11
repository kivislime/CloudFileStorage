package com.kivislime.filestorage.dto;

import java.io.InputStream;

public record FileUploadCommand(String ordinalName, String contentType, long size, InputStream inputStream) {
}
