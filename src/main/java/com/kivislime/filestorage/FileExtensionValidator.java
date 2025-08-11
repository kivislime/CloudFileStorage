package com.kivislime.filestorage;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
            throw new RuntimeException(ext);
        }
    }

    public void validateMimeType(InputStream inputStream) {
        try {
            inputStream.mark(512);
            String mime = detectMimeType(inputStream);
            inputStream.reset();

            if (!objectStorageProperties.allowedMimeTypes()
                    .contains(mime)) {
                throw new RuntimeException("MIME type " + mime + " is not allowed");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String detectMimeType(InputStream is) throws IOException {
        return URLConnection.guessContentTypeFromStream(is);
    }

}
