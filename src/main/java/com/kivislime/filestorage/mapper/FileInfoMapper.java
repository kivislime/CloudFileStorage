package com.kivislime.filestorage.mapper;

import com.kivislime.filestorage.dto.FileInfoResponse;
import com.kivislime.filestorage.entity.UserFile;
import com.kivislime.filestorage.util.ResourceParserUtil;
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
    FileInfoResponse toDto(UserFile userFile);

    List<FileInfoResponse> toDtoList(List<UserFile> userFiles);
}
