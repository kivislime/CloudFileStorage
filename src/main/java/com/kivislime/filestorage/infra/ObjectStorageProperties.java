package com.kivislime.filestorage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties("file")
public record ObjectStorageProperties(Set<String> allowedExtensions, Set<String> allowedMime) {
}
