package com.kivislime.filestorage.service;

import org.springframework.core.io.InputStreamResource;

public record FileDownloadResult(InputStreamResource stream, Long contentLength) {
}
