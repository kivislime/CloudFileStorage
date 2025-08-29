package com.kivislime.filestorage.service;

import com.kivislime.filestorage.dto.FileDownloadResult;
import com.kivislime.filestorage.dto.FileInfoResponse;
import com.kivislime.filestorage.dto.FileUploadCommand;
import com.kivislime.filestorage.dto.PresignedUrlResponse;
import com.kivislime.filestorage.entity.StorageItemType;
import com.kivislime.filestorage.entity.UserFile;
import com.kivislime.filestorage.exception.FileNotFoundException;
import com.kivislime.filestorage.exception.InvalidFileExtensionException;
import com.kivislime.filestorage.exception.InvalidResourceMoveException;
import com.kivislime.filestorage.exception.ObjectStorageException;
import com.kivislime.filestorage.mapper.FileInfoMapper;
import com.kivislime.filestorage.repository.FileRepositoryFacade;
import com.kivislime.filestorage.util.ResourceParserUtil;
import com.kivislime.filestorage.util.ZipResult;
import com.kivislime.filestorage.util.ZipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {
    private final ObjectStorageService objectStorageService;
    private final DirectoryService directoryService;
    private final FileRepositoryFacade fileRepository;
    private final FileInfoMapper fileInfoMapper;

    public FileInfoResponse getResourceInfo(Long userId, String path) {
        UserFile userFile = fileRepository.findByUserIdAndObjectKey(userId, path);
        return fileInfoMapper.toDto(userFile);
    }

    public List<FileInfoResponse> listResourcesRecursive(Long userId, String path) {
        List<UserFile> userFile = fileRepository.findByUserIdAndObjectKeyStartingWith(userId, path);
        return fileInfoMapper.toDtoList(userFile);
    }

    public List<FileInfoResponse> uploadResource(Long userId, String path, FileUploadCommand request) {
        String fullPath = path + request.ordinalName();
        String pathToCreate = path + ResourceParserUtil.getParentPath(request.ordinalName());
        log.info("User tried to upload resource for path=" + fullPath + ", userId=" + userId);

        UserFile newFile = fileRepository.saveFile(
                userId,
                fullPath,
                request.size(),
                StorageItemType.FILE);

        try {
            objectStorageService.upload(userId, fullPath, request);
        } catch (ObjectStorageException | InvalidFileExtensionException e) {
            fileRepository.deleteFile(newFile);
            throw e;
        }

        List<FileInfoResponse> directories = directoryService.createMissingDirectoriesForPath(pathToCreate, userId);
        directories.add(fileInfoMapper.toDto(newFile));
        return directories;
    }

    public List<FileInfoResponse> uploadResourceList(Long userId, String path, List<FileUploadCommand> request) {
        return request.stream()
                .map(f -> uploadResource(userId, path, f))
                .flatMap(Collection::stream)
                .toList();
    }

    public List<FileInfoResponse> moveResource(Long userId, String fromKey, String toKey) {
        if (fromKey.equals(toKey)) {
            throw new InvalidResourceMoveException("User " + userId + " tried to move/rename resource from " + fromKey + " to itself");
        }

        if (ResourceParserUtil.isDirectory(fromKey) && ResourceParserUtil.isDirectory(toKey)) {
            log.info("User tried to move resource from " + fromKey + " to " + toKey + ", userId=" + userId);
            return move(userId, fromKey, toKey);
        } else if (ResourceParserUtil.getNameFromPath(fromKey).equals(ResourceParserUtil.getNameFromPath(toKey))) {
            log.info("User tried to rename resource from " + fromKey + "to " + toKey + ", userId=" + userId);
            return rename(userId, fromKey, toKey);
        } else {
            throw new InvalidResourceMoveException("User " + userId +
                    " tried to move/rename resource with invalid path combination from " + fromKey + " to " + toKey);
        }
    }

    public PresignedUrlResponse generateDownloadUrl(Long userId, String path) {
        UserFile userFile = fileRepository.findByUserIdAndObjectKey(userId, path);
        return objectStorageService.getPresignedUrl(userFile.getUserId(), path);
    }

    public FileDownloadResult downloadResource(Long userId, String path) {
        List<UserFile> userFiles = fileRepository.findByUserIdAndObjectKeyStartingWith(userId, path);
        log.info("User tried to download resource from path=" + path + ", userId=" + userId);

        if (userFiles.isEmpty()) {
            throw new FileNotFoundException("No files found for userId=" + userId + ", path=" + path);
        }

        Map<String, Supplier<InputStream>> suppliers = userFiles.stream()
                .filter(f -> f.getStorageItemType() == StorageItemType.FILE)
                .collect(Collectors.toMap(
                        UserFile::getObjectKey,
                        f -> () -> objectStorageService.download(f.getUserId(), f.getObjectKey())));


        ZipResult userFilesArchive = ZipUtil.createArchiveHierarchy(
                suppliers,
                userFiles.stream()
                        .map(UserFile::getObjectKey)
                        .toList());

        return new FileDownloadResult(new InputStreamResource(userFilesArchive.stream()), userFilesArchive.length());
    }


    public void deleteResource(Long userId, String path) {
        log.info("User tried to delete resource from path=" + path + ", userId=" + userId);
        objectStorageService.deleteByPrefix(userId, path);
        if (fileRepository.deleteAllByUserIdAndObjectKey(userId, path) == 0) {
            throw new FileNotFoundException("No files found for userId=" + userId + ", path=" + path);
        }
    }

    public void cleanupOrphanFiles() {
        Map<Long, List<String>> list = objectStorageService.getAllObjectKeysByUserId();
        final int BATCH = 100;
        log.info("Start cleaning orphan files");

        for (Map.Entry<Long, List<String>> e : list.entrySet()) {
            Long userId = e.getKey();
            List<String> allKeys = e.getValue();
            for (int i = 0; i < allKeys.size(); i += BATCH) {
                int to = Math.min(i + BATCH, allKeys.size());
                List<String> batch = allKeys.subList(i, to);

                List<UserFile> present = fileRepository.findAllByUserIdAndObjectKeyIn(userId, batch);
                Set<String> presentKeys = present.stream()
                        .map(UserFile::getObjectKey)
                        .collect(Collectors.toSet());

                for (String key : batch) {
                    if (!presentKeys.contains(key)) {
                        try {
                            objectStorageService.delete(userId, key);
                            log.info("Deleted orphan object: user={}, key={}", userId, key);
                        } catch (Exception ex) {
                            log.warn("Failed to delete orphan object user={}, key={}, will retry later", userId, key, ex);
                        }
                    }
                }
            }
        }
        log.info("End cleaning orphan files");
    }

    private List<FileInfoResponse> move(Long userId, String fromKey, String toKey) {
        List<UserFile> files = fileRepository.findByUserIdAndObjectKeyStartingWith(userId, fromKey);
        Map<String, String> pairOldNewPath = new HashMap<>();

        for (UserFile file : files) {
            String newPath = toKey + file.getObjectKey().substring(fromKey.length());

            if (file.getStorageItemType() == StorageItemType.FILE) {
                pairOldNewPath.put(file.getObjectKey(), newPath);
            }
            file.setObjectKey(newPath);
        }
        tryCopyFiles(userId, pairOldNewPath);
        fileRepository.saveAll(files);
        tryDeleteFiles(userId, pairOldNewPath);

        return fileInfoMapper.toDtoList(files);
    }

    private void tryCopyFiles(Long userId, Map<String, String> pairOldNewPath) {
        for (Map.Entry<String, String> pair : pairOldNewPath.entrySet()) {
            try {
                objectStorageService.copy(userId, pair.getKey(), pair.getValue());
            } catch (ObjectStorageException copyEx) {
                log.error("Copy failed in update operation: {}", copyEx.getMessage());
                throw copyEx;
            }
        }
    }

    private void tryDeleteFiles(Long userId, Map<String, String> pairOldNewPath) {
        for (Map.Entry<String, String> pair : pairOldNewPath.entrySet()) {
            try {
                objectStorageService.delete(userId, pair.getKey());
            } catch (ObjectStorageException delEx) {
                log.warn("Failed to delete original object after successful copy. userId={}, fromKey={}, toKey={}",
                        userId, pair.getKey(), pair.getValue(), delEx);
            }
        }
    }

    private List<FileInfoResponse> rename(Long userId, String fromKey, String toKey) {
        UserFile file = fileRepository.updateFile(userId, fromKey, toKey);

        if (file.getStorageItemType() == StorageItemType.FILE) {
            tryCopyFiles(userId, Map.of(fromKey, toKey));
            tryDeleteFiles(userId, Map.of(fromKey, toKey));
        }

        List<FileInfoResponse> list = directoryService.createMissingDirectoriesForPath(toKey, userId);
        list.add(fileInfoMapper.toDto(file));
        return list;
    }

}
