package com.kivislime.filestorage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MinioInitializer implements CommandLineRunner {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioInitializer(MinioClient minioClient,
                            MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @Override
    public void run(String... args) throws Exception {
        String bucketName = minioProperties.bucketName();

        if (!minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build())
        ) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            log.info("Bucket {} created", bucketName);
        } else {
            log.info("Bucket {} already exists", bucketName);
        }
    }
}
