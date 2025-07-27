package com.kivislime.filestorage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FileService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final FileRepository userFilesRepository;
    private final ObjectMapper mapper;

    //TODO: запретить загрузку исполняемых файлов и файлов размер максимальный на юзера 10 кб
    public FileInfoDto getFileInfo(Long userId, String path) {
        //TODO: может нет смысла доставать сначала отсюда объект? Может сразу толкать в minio? Не лишний ли запрос в бд?
        UserFile userFile = userFilesRepository.getUserFileByUserIdAndObjectKey(userId, path)
                .orElseThrow(() -> new RuntimeException(String.format("User with id: %s, file %s not found", userId, path)));

        if (userFile.getStorageItemType() == StorageItemType.DIRECTORY) {
            throw new RuntimeException(String.format("User with id: %s, request directory: %s, bit it is not directory", userId, path));
        }

        try {
            String objectKey = userFile.getObjectKey();
            StatObjectResponse statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(objectKey)
                    .build());

            return new FileInfoDto(
                    FileParserUtil.findFileName(objectKey),
                    FileParserUtil.findPathToFile(objectKey),
                    statObjectResponse.size(),
                    StorageItemType.FILE);
        } catch (Exception e) {
            throw new RuntimeException("Cannot stat object " + path, e); //TODO: MinioException
        }
    }

    public List<FileInfoDto> getFileList(String path, Long userId) {
        if (!path.endsWith("/")) {
            throw new IllegalArgumentException("Path must end with /");
        }

        List<UserFile> userFile = userFilesRepository.findByUserIdAndObjectKeyStartingWith(userId, path);

        return mapper.convertValue(userFile, new TypeReference<>() {
        });
    }

    public List<FileInfoDto> uploadFile(Long userId, String path, FileUploadRequest fileUploadRequest) {
        //TODO: Рекурсивное создание, если нет пути - создать
        //TODO: Если на входе путь без '/'? обработать
        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setObjectKey(path + fileUploadRequest.ordinalName());
        userFile.setStorageItemType(StorageItemType.FILE);
        userFilesRepository.save(userFile);

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.bucketName())
                    .object(path + fileUploadRequest.ordinalName())
                    .stream(new ByteArrayInputStream(fileUploadRequest.bytes()), fileUploadRequest.size(), -1)
                    .contentType(fileUploadRequest.contentType())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Cannot upload file " + path + fileUploadRequest, e);
        }

        return List.of(new FileInfoDto(path, fileUploadRequest.ordinalName(), fileUploadRequest.size(), StorageItemType.FILE));
    }
}
