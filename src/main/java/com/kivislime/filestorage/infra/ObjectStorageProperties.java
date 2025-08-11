package com.kivislime.filestorage.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties("file")
public record ObjectStorageProperties(Set<String> allowedExtensions, Set<String> allowedMime) {
}
