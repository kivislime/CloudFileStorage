package com.kivislime.filestorage;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring",
        imports = {ResourceParserUtil.class})
public interface FileInfoMapper {
    @Mapping(target = "path", expression = "java(ResourceParserUtil.getParentPath(userFile.getObjectKey()))")
    @Mapping(target = "name", expression = "java(ResourceParserUtil.getNameFromPath(userFile.getObjectKey()))")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "type", source = "storageItemType")
    FileInfoDto toDto(UserFile userFile);

    List<FileInfoDto> toDtoList(List<UserFile> userFiles);
}
