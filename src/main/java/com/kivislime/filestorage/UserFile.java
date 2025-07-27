package com.kivislime.filestorage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

//TODO: избавиться от висячих ссылок? ввести  ON DELETE CASCADE;
// default FILE?
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_files")
public class UserFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "storage_item_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StorageItemType storageItemType;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;
}