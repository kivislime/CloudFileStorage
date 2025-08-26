package com.kivislime.filestorage.dto;

import java.io.InputStream;
import java.util.function.Supplier;

public record FileUploadCommand(String ordinalName, String contentType, long size, Supplier<InputStream> supplierStream) {
}
