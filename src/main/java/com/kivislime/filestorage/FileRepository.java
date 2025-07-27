package com.kivislime.filestorage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<UserFile, Long> {
    List<UserFile> findByUserIdAndObjectKeyStartingWith(Long userId, String path);

    Optional<UserFile> getUserFileByUserIdAndObjectKey(Long userId, String path);
}
