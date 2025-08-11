package com.kivislime.filestorage.dto;

import com.kivislime.filestorage.entity.StorageItemType;

public record FileInfoResponse(String path, String name, Long size, StorageItemType type) {
}
