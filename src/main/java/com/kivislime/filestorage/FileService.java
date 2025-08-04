package com.kivislime.filestorage;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FileService {
    private final ObjectStorageService objectStorageService;
    private final FileRepository userFilesRepository;
    private final FileInfoMapper fileInfoMapper;

    //TODO: запретить загрузку исполняемых файлов и файлов размер максимальный на юзера 10 кб
    public FileInfoDto getFileInfo(Long userId, String path) {
        //TODO: может нет смысла доставать сначала отсюда объект? Может сразу толкать в minio? Не лишний ли запрос в бд?
        UserFile userFile = userFilesRepository.findByUserIdAndObjectKey(userId, path)
                .orElseThrow(() -> new RuntimeException(String.format("User with id: %s, file %s not found", userId, path)));

        if (userFile.getStorageItemType() == StorageItemType.DIRECTORY) {
            throw new RuntimeException(String.format("User with id: %s, request directory: %s, bit it is not directory", userId, path));
        }

        String objectKey = userFile.getObjectKey();
        return new FileInfoDto(
                FileParserUtil.getParentPath(objectKey),
                FileParserUtil.getNameFromPath(objectKey),
                userFile.getSize(),
                StorageItemType.FILE);
    }

    public List<FileInfoDto> getFileList(Long userId, String path) {
        //TODO: Нет тоже проверки на файл/директорию по входящему path. Эта норм?
        if (!path.endsWith("/")) {
            throw new IllegalArgumentException("Path must end with /");
        }
        List<UserFile> userFile = userFilesRepository.findByUserIdAndObjectKeyStartingWith(userId, path);

        return fileInfoMapper.toDtoList(userFile);
    }

    //TODO: Нет тоже проверки на файл/директорию по входящему path. Если придет директория, но с путем /../path, то забагается из-за отсутствия '/'
    public List<FileInfoDto> uploadFile(Long userId, String path, FileUploadRequest request) {
        UserFile newFile = new UserFile();
        String fullPath = path + request.ordinalName();
        newFile.setUserId(userId);
        newFile.setObjectKey(fullPath);
        newFile.setSize(request.size());
        newFile.setStorageItemType(StorageItemType.FILE);

        try {
            userFilesRepository.save(newFile);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("User already exists", e);
        }
        List<UserFile> directories = createMissingDirectoriesForPath(path, userId);
        directories.add(newFile);

        objectStorageService.upload(userId, path, request);

        return fileInfoMapper.toDtoList(directories);
    }

    @Transactional
    public void deleteFile(Long userId, String path) {
        //TODO: сделать рекурсивным? Создать второй метод и эндпоинт, дающий возможность удалять рекурсивно?
        // Удалять по началу пути в бд. А в minio удалять по префиксу начала пути(если такое работает там)
        objectStorageService.delete(userId, path);
        userFilesRepository.deleteByUserIdAndObjectKey(userId, path);
    }

    @Transactional
    public FileInfoDto moveFile(Long userId, String fromKey, String toKey) {
        //TODO: Оптимальный это ли способ? Пришлось делать 4(2 в создании директорий) запроса к бд
        UserFile userFile = userFilesRepository.findByUserIdAndObjectKey(userId, fromKey)
                .orElseThrow(() -> new RuntimeException("User with id: " + userId + " not found"));

        objectStorageService.copy(userId, fromKey, toKey);
        objectStorageService.delete(userId, fromKey);

        String directoryPath = FileParserUtil.getParentPath(toKey);
        createMissingDirectoriesForPath(directoryPath, userId);

        userFile.setObjectKey(toKey);
        userFilesRepository.save(userFile);

        return fileInfoMapper.toDto(userFile);
    }

    //TODO: протестировать, оптимизировать
    public FileInfoDto renameFile(Long userId, String fromKey, String toKey) {
        UserFile userFile = userFilesRepository.findByUserIdAndObjectKey(userId, fromKey)
                .orElseThrow(() -> new RuntimeException("User with id: " + userId + " not found"));

        objectStorageService.copy(userId, fromKey, toKey);
        objectStorageService.delete(userId, fromKey);

        userFile.setObjectKey(toKey);
        userFilesRepository.save(userFile);

        return fileInfoMapper.toDto(userFile);
    }

    public PresignedUrlDto downloadFileByUrl(Long userId, String path) {
        UserFile userFile = userFilesRepository.findByUserIdAndObjectKey(userId, path)
                .orElseThrow(() -> new RuntimeException("User with id: " + userId + " not found"));
        return objectStorageService.getPresignedUrl(userFile.getUserId(), path);
    }

    public FileDownloadDto download(Long userId, String path) {
        List<UserFile> userFiles = userFilesRepository.findByUserIdAndObjectKeyStartingWith(userId, path);
        if (userFiles.isEmpty()) {
            throw new RuntimeException("User files with id: " + userId + " not found");
        }

        Map<String, InputStream> downloadedFileStreams = new HashMap<>();
        UserFile test = null;
        for (UserFile userFile : userFiles) {
            if (userFile.getStorageItemType() == StorageItemType.FILE) {
                String objectKey = userFile.getObjectKey();
                InputStream fileStream = objectStorageService.download(userFile.getUserId(), objectKey);
                downloadedFileStreams.put(objectKey, fileStream);
                test = userFile;
            }
        }
        if (test == null) {
            throw new RuntimeException("User files with id: " + userId + " not found");
        }
        ZipResult userFilesArchive = ZipUtils.createArchiveHierarchy(downloadedFileStreams, userFiles);
        return new FileDownloadDto(new InputStreamResource(userFilesArchive.stream()), userFilesArchive.length());
    }

    //TODO: добавить комментарий, что передаются исключительно пути директорий?
    // этот метод надо будет продублировать в directoryService. как правильно сделать? Говорили, что можно пренебречь clean code, так как сервисы все-таки
    // работают по разному и не факт, что создание списка директории не поменятеся и не станет не тем, который был нужен изначально
    private List<UserFile> createMissingDirectoriesForPath(String directoryPath, Long userId) {
        List<String> prefixes = buildPrefixes(directoryPath);
        List<UserFile> existing = userFilesRepository.findByUserIdAndObjectKeyInAndStorageItemType(
                userId,
                prefixes,
                StorageItemType.DIRECTORY);

        Set<String> existingKeys = existing.stream()
                .map(UserFile::getObjectKey)
                .collect(Collectors.toSet());

        List<UserFile> toCreate = prefixes.stream()
                .filter(s -> !existingKeys.contains(s))
                .map(s -> {
                    UserFile uf = new UserFile();
                    uf.setUserId(userId);
                    uf.setObjectKey(s);
                    uf.setStorageItemType(StorageItemType.DIRECTORY);
                    return uf;
                })
                .toList();

        if (!toCreate.isEmpty()) {
            userFilesRepository.saveAll(toCreate);
        }

        List<UserFile> result = new ArrayList<>(existing.size() + toCreate.size() + 1);
        result.addAll(existing);
        result.addAll(toCreate);

        return result;
    }

    private List<String> buildPrefixes(String path) {
        String[] parts = path.split("/");
        List<String> prefixes = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(part).append("/");
            prefixes.add(sb.toString());
        }
        return prefixes;
    }


}
