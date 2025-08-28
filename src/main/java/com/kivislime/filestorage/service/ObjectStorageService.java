package com.kivislime.filestorage.service;

import com.kivislime.filestorage.dto.FileUploadCommand;
import com.kivislime.filestorage.infra.MinioProperties;
import com.kivislime.filestorage.dto.PresignedUrlResponse;
import com.kivislime.filestorage.exception.ObjectStorageException;
import com.kivislime.filestorage.infra.validators.FileExtensionValidator;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@Slf4j
@RequiredArgsConstructor
@Service
public class ObjectStorageService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final FileExtensionValidator fileExtensionValidator;

    public void upload(Long userId, String path, FileUploadCommand request) {
        fileExtensionValidator.validateExtension(request.ordinalName());

        try (InputStream isRaw = request.supplierStream().get();
             BufferedInputStream is = new BufferedInputStream(isRaw, 8192)) {

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(buildObjectKey(userId, path))
                    .stream(is, request.size(), -1)
                    .build());
        } catch (Exception e) {
            throw new ObjectStorageException("File for user with id: " + userId + " cannot be uploaded, by path: " + path, e);
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
            throw new ObjectStorageException("File for user with id: " + userId + " cannot be created url, by path: " + path, e);
        }
    }

    public InputStream download(Long userId, String path) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(buildObjectKey(userId, path))
                    .build());
        } catch (Exception e) {
            throw new ObjectStorageException("File for user with id: " + userId + " cannot be downloaded, by path: " + path, e);
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
            throw new ObjectStorageException(String.format("File for user with id: %s cannot be copied from %s to %s", userId, fromKey, toKey), e);
        }
    }

    public void delete(Long userId, String path) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(buildObjectKey(userId, path))
                    .build());
        } catch (Exception e) {
            throw new ObjectStorageException("File for user with id: " + userId + " cannot be deleted, by path: " + path, e);
        }
    }

    public void deleteByPrefix(Long userId, String path) {
        try {
            String prefix = buildObjectKey(userId, path);

            Iterable<Result<Item>> objects = minioClient.listObjects(
                    io.minio.ListObjectsArgs.builder()
                            .bucket(minioProperties.bucketName())
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );
            List<DeleteObject> toDelete = StreamSupport.stream(objects.spliterator(), false)
                    .map(itemResult -> {
                        try {
                            return new DeleteObject(itemResult.get().objectName());
                        } catch (Exception e) {
                            throw new ObjectStorageException("Cannot process listed item for deletion, skipping ", e);
                        }
                    })
                    .toList();

            if (!toDelete.isEmpty()) {
                minioClient.removeObjects(RemoveObjectsArgs.builder()
                        .bucket(minioProperties.bucketName())
                        .objects(toDelete)
                        .build());
            }
        } catch (Exception e) {
            throw new ObjectStorageException("Objects for user with id: " + userId + " cannot be deleted by prefix: " + path, e);
        }
    }

    public Map<Long, List<String>> getAllObjectKeysByUserId() {
        Iterable<Result<Item>> iterable = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioProperties.bucketName())
                        .recursive(true)
                        .build()
        );

        Pattern digits = Pattern.compile("(\\d+)");
        Map<Long, List<String>> objectKeys = new HashMap<>();

        for (Result<Item> result : iterable) {
            try {
                Item item = result.get();
                String objectName = item.objectName();

                int index = objectName.indexOf('/');
                if (index <= 0) {
                    log.warn("Skipping object with unexpected format (no prefix): {}", objectName);
                    continue;
                }

                String prefix = objectName.substring(0, index);
                Matcher m = digits.matcher(prefix);
                if (!m.find()) {
                    log.warn("Cannot extract userId from prefix '{}', object '{}'", prefix, objectName);
                    continue;
                }
                Long userId = Long.parseLong(m.group(1));

                String key = objectName.substring(index + 1);
                objectKeys.computeIfAbsent(userId, k -> new ArrayList<>()).add(key);
            } catch (Exception e) {
                log.error("Failed to process object listing item, skipping {}", e.getMessage());
            }
        }
        return objectKeys;
    }


    private String buildObjectKey(Long userId, String fullPath) {
        return String.format(minioProperties.objectPathPattern(), userId, fullPath);
    }
}
