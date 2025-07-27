package com.kivislime.filestorage;

public record FileInfoDto(String path, String name, Long size, StorageItemType type) {
}
