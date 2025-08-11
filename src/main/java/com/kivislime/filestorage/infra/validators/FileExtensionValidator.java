package com.kivislime.filestorage.infra;

import com.kivislime.filestorage.exception.DetectMimeTypeException;
import com.kivislime.filestorage.exception.InvalidFileExtensionException;
import com.kivislime.filestorage.exception.InvalidMimeTypeException;
import com.kivislime.filestorage.util.ResourceParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

@RequiredArgsConstructor
@Component
public class FileExtensionValidator {
    private final ObjectStorageProperties objectStorageProperties;

    public void validateExtension(String filename) {
        String ext = ResourceParserUtil.getExtension(filename);
        if (!objectStorageProperties.allowedExtensions()
                .contains(ext)) {
            throw new InvalidFileExtensionException("filename: " + ext);
        }
    }

    public void validateMimeType(InputStream inputStream) {
        try {
            inputStream.mark(512);
            String mime = detectMimeType(inputStream);
            inputStream.reset();

            if (!objectStorageProperties.allowedMime()
                    .contains(mime)) {
                throw new InvalidMimeTypeException("MIME type " + mime + " is not allowed");
            }
        } catch (IOException e) {
            throw new DetectMimeTypeException("Cannot detect mime type" + e);
        }
    }

    private String detectMimeType(InputStream is) throws IOException {
        return URLConnection.guessContentTypeFromStream(is);
    }

}
