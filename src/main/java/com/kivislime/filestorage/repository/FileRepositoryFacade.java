package com.kivislime.filestorage;

import com.kivislime.filestorage.exception.FileAlreadyExistsException;
import com.kivislime.filestorage.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class FileRepositoryFacade {
    private final FileRepository fileRepository;

    public UserFile findByUserIdAndObjectKey(Long userId, String path) {
        return fileRepository.findByUserIdAndObjectKey(userId, path)
                .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));
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
            throw new FileAlreadyExistsException("File already exists", e);
        }
    }

    @Transactional
    public UserFile updateFile(Long userId, String fromKey, String toKey) {
        UserFile userFile = fileRepository.findByUserIdAndObjectKey(userId, fromKey)
                .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));

        userFile.setObjectKey(toKey);
        try {
            return fileRepository.save(userFile);
        } catch (DataIntegrityViolationException e) {
            throw new FileAlreadyExistsException("File already exists", e);
        }
    }

    @Transactional
    public void deleteByUserIdAndObjectKey(Long userId, String path) {
        fileRepository.deleteByUserIdAndObjectKey(userId, path);
    }
}
