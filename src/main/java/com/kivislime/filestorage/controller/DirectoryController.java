package com.kivislime.filestorage.controller;

import com.kivislime.filestorage.dto.FileInfoResponse;
import com.kivislime.filestorage.security.UserPrincipal;
import com.kivislime.filestorage.service.DirectoryService;
import com.kivislime.filestorage.validation.ValidDirectoryPath;
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
    public ResponseEntity<List<FileInfoResponse>> getDirectory(@RequestParam @ValidDirectoryPath String path,
                                                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<FileInfoResponse> fileList = directoryService.listDirectChildren(userPrincipal.getId(), path);
        return ResponseEntity.ok(fileList);
    }

    @PostMapping
    public ResponseEntity<List<FileInfoResponse>> createDirectory(@RequestParam @ValidDirectoryPath String path,
                                                                  @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<FileInfoResponse> fileList = directoryService.createDirectoryStrict(userPrincipal.getId(), path);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileList);
    }
}
