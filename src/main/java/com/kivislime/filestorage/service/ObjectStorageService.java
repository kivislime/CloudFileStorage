package com.kivislime.filestorage.service;

import com.kivislime.filestorage.dto.FileUploadCommand;
import com.kivislime.filestorage.infra.MinioProperties;
import com.kivislime.filestorage.dto.PresignedUrlResponse;
import com.kivislime.filestorage.exception.ObjectStorageException;
import com.kivislime.filestorage.infra.validators.FileExtensionValidator;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class ObjectStorageService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final FileExtensionValidator fileExtensionValidator;

    public void upload(Long userId, String path, FileUploadCommand request) {
        fileExtensionValidator.validateExtension(path);
        fileExtensionValidator.validateMimeType(request.inputStream());

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(buildObjectKey(userId, path + request.ordinalName()))
                    .stream(request.inputStream(), request.size(), -1)
                    .build());
        } catch (Exception e) {
            throw new ObjectStorageException("Cannot upload file " + path + request, e);
        }
    }

    public PresignedUrlResponse getPresignedUrl(Long userId, String path) {
        try {
            int expirySeconds = Math.toIntExact(minioProperties.presignedUrlTtl().toSeconds());
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioProperties.bucketName())
                            .object(buildObjectKey(userId, path))
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );
            return new PresignedUrlResponse(url, expirySeconds);
        } catch (Exception e) {
            throw new ObjectStorageException("Cannot create url to file: " + path, e);
        }
    }

    public InputStream download(Long userId, String path) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(buildObjectKey(userId, path))
                    .build());
        } catch (Exception e) {
            throw new ObjectStorageException("Cannot download file: " + path, e);
        }
    }

    public void copy(Long userId, String fromKey, String toKey) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(minioProperties.bucketName())
                            .object(buildObjectKey(userId, toKey))
                            .source(CopySource.builder()
                                    .bucket(minioProperties.bucketName())
                                    .object(buildObjectKey(userId, fromKey))
                                    .build())
                            .build()
            );
        } catch (Exception e) {
            throw new ObjectStorageException("Cannot copy file from" + fromKey + " to " + toKey, e);
        }
    }

    public void delete(Long userId, String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(buildObjectKey(userId, path))
                    .build());
        } catch (Exception e) {
            throw new ObjectStorageException("Cannot delete file " + path, e);
        }
    }

    private String buildObjectKey(Long userId, String fullPath) {
        return String.format(minioProperties.objectPathPattern(), userId, fullPath);
    }
}
