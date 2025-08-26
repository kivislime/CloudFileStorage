package com.kivislime.filestorage.repository;

import com.kivislime.filestorage.entity.StorageItemType;
import com.kivislime.filestorage.entity.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<UserFile, Long> {
    Optional<UserFile> findByUserIdAndObjectKey(Long userId, String path);

    List<UserFile> findByUserIdAndObjectKeyStartingWith(Long userId, String path);

    List<UserFile> findByUserIdAndObjectKeyInAndStorageItemType(Long userId, List<String> path, StorageItemType storageItemType);

    @Query("""
            SELECT uf
            FROM UserFile uf
            WHERE uf.userId = :userId
              AND (
                (uf.objectKey LIKE CONCAT(:path, '%')
                 AND uf.objectKey NOT LIKE CONCAT(:path, '%', '/', '%')
                )
                OR
                (uf.objectKey LIKE CONCAT(:path, '%/')
                 AND uf.objectKey NOT LIKE CONCAT(:path, '%/%', '/', '%')
                )
              )
            """)
    List<UserFile> findDirectChildren(Long userId, String path);

    @Modifying(clearAutomatically = true)
    int deleteByUserIdAndObjectKeyStartingWith(Long userId, String prefix);

    boolean existsByUserIdAndObjectKey(Long userId, String objectKey);

    List<UserFile> findAllByUserIdAndObjectKeyIn(Long userId, List<String> objectKey);

    boolean existsByUserIdAndObjectKeyAndStorageItemType(Long userId, String path, StorageItemType storageItemType);

    List<UserFile> findByUserIdAndStorageItemTypeAndObjectKeyStartingWith(Long userId, StorageItemType type, String fromKey);
}
