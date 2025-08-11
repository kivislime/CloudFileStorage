package com.kivislime.filestorage.dto;

import com.kivislime.filestorage.entity.StorageItemType;

public record FileInfoDto(String path, String name, Long size, StorageItemType type) {
}
