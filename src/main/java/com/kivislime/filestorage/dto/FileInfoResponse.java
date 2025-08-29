package com.kivislime.filestorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kivislime.filestorage.entity.StorageItemType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FileInfoResponse(String path, String name, Long size, StorageItemType type) {
}
