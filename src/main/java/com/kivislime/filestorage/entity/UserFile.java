package com.kivislime.filestorage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_files", uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"user_id", "object_key"}
        )})
public class UserFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "size", nullable = false)
    private Long size = 0L;

    @Column(name = "storage_item_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StorageItemType storageItemType;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;
}