package com.kivislime.filestorage;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@RequiredArgsConstructor
@Service
public class FileService {
    private final ObjectStorageService objectStorageService;
    private final DirectoryService directoryService;
    private final FileRepositoryFacade fileRepository;
    private final FileInfoMapper fileInfoMapper;

    //TODO:  размер максимальный на юзера 10 кб
    public FileInfoDto getResourceInfo(Long userId, String path) {
        UserFile userFile = fileRepository.findByUserIdAndObjectKey(userId, path);
        return fileInfoMapper.toDto(userFile);
    }

    public List<FileInfoDto> listResourcesRecursive(Long userId, String path) {
        List<UserFile> userFile = fileRepository.findByUserIdAndObjectKeyStartingWith(userId, path);
        return fileInfoMapper.toDtoList(userFile);
    }

    public List<FileInfoDto> uploadResource(Long userId, String path, FileUploadRequest request) {
        UserFile newFile = fileRepository.saveFile(
                userId,
                path + request.ordinalName(),
                request.size(),
                StorageItemType.FILE);

        objectStorageService.upload(userId, path, request);

        List<FileInfoDto> directories = directoryService.createMissingDirectoriesForPath(path, userId);
        directories.add(fileInfoMapper.toDto(newFile));

        return directories;
    }

    public FileInfoDto moveResource(Long userId, String fromKey, String toKey) {
        UserFile userFile = fileRepository.updateFile(userId, fromKey, toKey);
        String nameFrom = ResourceParserUtil.getNameFromPath(fromKey);
        String nameTo = ResourceParserUtil.getNameFromPath(toKey);

        if (nameFrom.equals(nameTo)) {
            directoryService.createMissingDirectoriesForPath(nameTo, userId);
        }

        objectStorageService.copy(userId, fromKey, toKey);
        objectStorageService.delete(userId, fromKey);

        return fileInfoMapper.toDto(userFile);
    }

    public PresignedUrlDto generateDownloadUrl(Long userId, String path) {
        UserFile userFile = fileRepository.findByUserIdAndObjectKey(userId, path);
        return objectStorageService.getPresignedUrl(userFile.getUserId(), path);
    }

    public FileDownloadDto downloadResource(Long userId, String path) {
        List<UserFile> userFiles = fileRepository.findByUserIdAndObjectKeyStartingWith(userId, path);
        if (userFiles.isEmpty()) {
            throw new FileNotFoundException("User files with id: " + userId + " not found");
        }

        Map<String, InputStream> downloadedFileStreams = new HashMap<>();
        for (UserFile userFile : userFiles) {
            if (userFile.getStorageItemType() == StorageItemType.FILE) {
                String objectKey = userFile.getObjectKey();
                InputStream fileStream = objectStorageService.download(userFile.getUserId(), objectKey);
                downloadedFileStreams.put(objectKey, fileStream);
            }
        }

        ZipResult userFilesArchive = ZipUtil.createArchiveHierarchy(downloadedFileStreams, userFiles);
        return new FileDownloadDto(new InputStreamResource(userFilesArchive.stream()), userFilesArchive.length());
    }


    public void deleteResource(Long userId, String path) {
        //TODO: сделать рекурсивным? Создать второй метод и эндпоинт, дающий возможность удалять рекурсивно?
        // Удалять по началу пути в бд. А в minio удалять по префиксу начала пути(если такое работает там)
        fileRepository.deleteByUserIdAndObjectKey(userId, path);
        objectStorageService.delete(userId, path);
    }

}
