package com.kivislime.filestorage;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/directory")
public class DirectoryController {
    private final DirectoryService directoryService;

    @GetMapping
    public ResponseEntity<List<FileInfoDto>> getDirectory(@RequestParam @ValidDirectoryPath String path,
                                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<FileInfoDto> fileList = directoryService.listDirectChildren(userPrincipal.getId(), path);
        return new ResponseEntity<>(fileList, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<List<FileInfoDto>> createDirectory(@RequestParam @ValidDirectoryPath String path,
                                                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<FileInfoDto> fileList = directoryService.createMissingDirectoriesForPath(path, userPrincipal.getId());
        return new ResponseEntity<>(fileList, HttpStatus.CREATED);
    }
}
