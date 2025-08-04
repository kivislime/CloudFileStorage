package com.kivislime.filestorage;

public record PresignedUrlDto(String url, long expiresInSeconds) {
}
