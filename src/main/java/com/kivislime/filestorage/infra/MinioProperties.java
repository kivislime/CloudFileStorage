package com.kivislime.filestorage.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "minio")
public record MinioProperties(String url,
                              String externalUrl,
                              String accessKey,
                              String secretKey,
                              String bucketName,
                              String objectPathPattern,
                              Duration presignedUrlTtl) {
}
