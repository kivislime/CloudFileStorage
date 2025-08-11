package com.kivislime.filestorage.service;

import com.kivislime.filestorage.dto.FileInfoResponse;
import com.kivislime.filestorage.entity.StorageItemType;
import com.kivislime.filestorage.entity.UserFile;
import com.kivislime.filestorage.mapper.FileInfoMapper;
import com.kivislime.filestorage.repository.FileRepository;
import com.kivislime.filestorage.util.ResourceParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DirectoryService {
    private final FileRepository fileRepository;
    private final FileInfoMapper fileInfoMapper;

    public List<FileInfoResponse> listDirectChildren(Long userId, String path) {
        List<UserFile> fileList = fileRepository.findDirectChildren(userId, path);
        return fileInfoMapper.toDtoList(fileList);
    }

    @Transactional
    public List<FileInfoResponse> createMissingDirectoriesForPath(String directoryPath, Long userId) {
        List<String> prefixes = ResourceParserUtil.buildPrefixes(directoryPath);
        List<UserFile> existing = fileRepository.findByUserIdAndObjectKeyInAndStorageItemType(
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
            fileRepository.saveAll(toCreate);
        }

        List<UserFile> result = new ArrayList<>(existing.size() + toCreate.size() + 1);
        result.addAll(existing);
        result.addAll(toCreate);

        return fileInfoMapper.toDtoList(result);
    }
}
