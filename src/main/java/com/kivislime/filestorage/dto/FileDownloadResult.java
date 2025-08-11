package com.kivislime.filestorage.dto;

import org.springframework.core.io.InputStreamResource;

public record FileDownloadResult(InputStreamResource stream, Long contentLength) {
}
