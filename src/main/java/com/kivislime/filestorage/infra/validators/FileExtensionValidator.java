package com.kivislime.filestorage.infra.validators;

import com.kivislime.filestorage.exception.InvalidFileExtensionException;
import com.kivislime.filestorage.infra.ObjectStorageProperties;
import com.kivislime.filestorage.util.ResourceParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FileExtensionValidator {
    private final ObjectStorageProperties objectStorageProperties;

    public void validateExtension(String filename) {
        String ext = ResourceParserUtil.getExtension(filename);
        if (!objectStorageProperties.allowedExtensions()
                .contains(ext)) {
            throw new InvalidFileExtensionException("Invalid file extension: " + ext + ", for path: " + filename);
        }
    }
}
