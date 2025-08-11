package com.kivislime.filestorage.dto;

public record PresignedUrlDto(String url, long expiresInSeconds) {
}
