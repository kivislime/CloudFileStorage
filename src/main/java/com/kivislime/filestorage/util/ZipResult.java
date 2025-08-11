package com.kivislime.filestorage.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public record ZipResult(byte[] data, long length) {
    public InputStream stream() {
        return new ByteArrayInputStream(data);
    }
}
