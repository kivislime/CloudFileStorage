    package com.kivislime.filestorage;

    import io.minio.MinioClient;
    import lombok.RequiredArgsConstructor;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;

    @RequiredArgsConstructor
    @Configuration
    public class MinioConfiguration {
        private final MinioProperties properties;

        @Bean
        public MinioClient minioClient() {
            return MinioClient.builder()
                    .endpoint(properties.url())
                    .credentials(properties.accessKey(), properties.secretKey())
                    .build();
        }
    }
