package com.kivislime.filestorage.dto;

import com.kivislime.filestorage.entity.StorageItemType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FileInfoResponse(String path, String name, Long size, StorageItemType type) {
}
