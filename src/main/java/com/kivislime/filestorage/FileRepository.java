package com.kivislime.filestorage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//TODO: enableJpaRepositories in config?
@Repository
public interface FileRepository extends JpaRepository<UserFile, Long> {
    Optional<UserFile> findByUserIdAndObjectKey(Long userId, String path);

    List<UserFile> findByUserIdAndObjectKeyStartingWith(Long userId, String path);

    List<UserFile> findByUserIdAndObjectKeyInAndStorageItemType(Long userId, List<String> path, StorageItemType storageItemType);

//    @Modifying
//    @Query("UPDATE UserFile uf SET uf.objectKey = :newPath WHERE uf.id = :id")
//    int updatePathById(@Param("id") Long id, @Param("newPath") String newPath);

    void deleteByUserIdAndObjectKey(Long userId, String path);
}
