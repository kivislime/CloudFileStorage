package com.kivislime.filestorage.repository;

import com.kivislime.filestorage.entity.StorageItemType;
import com.kivislime.filestorage.entity.UserFile;
import com.kivislime.filestorage.exception.FileAlreadyExistsException;
import com.kivislime.filestorage.exception.UserFileNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class FileRepositoryFacade {
    private final FileRepository fileRepository;

    public UserFile findByUserIdAndObjectKey(Long userId, String path) {
        return fileRepository.findByUserIdAndObjectKey(userId, path)
                .orElseThrow(() -> new UserFileNotFoundException("File for user with id: " + userId + " not found, by path: " + path));
    }

    public List<UserFile> findByUserIdAndObjectKeyStartingWith(Long userId, String path) {
        return fileRepository.findByUserIdAndObjectKeyStartingWith(userId, path);
    }

    @Transactional
    public UserFile saveFile(Long userId, String objectKey, Long size, StorageItemType type) {
        UserFile newFile = new UserFile();
        newFile.setUserId(userId);
        newFile.setObjectKey(objectKey);
        newFile.setSize(size);
        newFile.setStorageItemType(type);

        try {
            return fileRepository.save(newFile);
        } catch (DataIntegrityViolationException e) {
            throw new FileAlreadyExistsException("File for user with id: " + userId + " already exists, by path: " + objectKey, e);
        }
    }

    @Transactional
    public UserFile updateFile(Long userId, String fromKey, String toKey) {
        UserFile userFile = fileRepository.findByUserIdAndObjectKey(userId, fromKey)
                .orElseThrow(() -> new UserFileNotFoundException("File for user with id: " + userId + " not found, by path: " + fromKey));

        userFile.setObjectKey(toKey);
        try {
            return fileRepository.saveAndFlush(userFile);
        } catch (DataIntegrityViolationException e) {
            throw new FileAlreadyExistsException("File for user with id: " + userId + " already exists, by path: " + toKey, e);
        }
    }

    @Transactional
    public void saveAll(List<UserFile> files) {
        try {
            fileRepository.saveAll(files);
            fileRepository.flush();
        } catch (DataIntegrityViolationException e) {
            Long userId = files.isEmpty() ? null : files.get(0).getUserId();
            String objectKeys = files.stream()
                    .map(UserFile::getObjectKey)
                    .collect(Collectors.joining(", "));

            throw new FileAlreadyExistsException("File(s) already exist for userId=" + userId + ", objectKeys=[" + objectKeys + "]", e);
        }
    }


    @Transactional
    public void deleteFile(UserFile userFile) {
        fileRepository.delete(userFile);
    }

    @Transactional
    public int deleteAllByUserIdAndObjectKey(Long userId, String objectKey) {
        return fileRepository.deleteByUserIdAndObjectKeyStartingWith(userId, objectKey);
    }

    public List<UserFile> findAllByUserIdAndObjectKeyIn(Long userId, List<String> objectKey) {
        return fileRepository.findAllByUserIdAndObjectKeyIn(userId, objectKey);
    }

    public List<UserFile> findByUserIdAndStorageItemTypeAndObjectKeyStartingWith(Long userId, StorageItemType type, String fromKey) {
        return fileRepository.findByUserIdAndStorageItemTypeAndObjectKeyStartingWith(userId, type, fromKey);
    }

    public boolean userFileExists(Long userId, String toKey) {
        return fileRepository.existsByUserIdAndObjectKey(userId, toKey);
    }

}
