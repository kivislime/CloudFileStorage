package com.kivislime.filestorage;

import org.springframework.core.io.InputStreamResource;

public record FileDownloadDto(InputStreamResource stream, Long contentLength) {
}
