package com.kivislime.filestorage.dto;

public record PresignedUrlResponse(String url, long expiresInSeconds) {
}
