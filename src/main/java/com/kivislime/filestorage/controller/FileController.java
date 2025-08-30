package com.kivislime.filestorage.controller;

import com.kivislime.filestorage.dto.FileDownloadResult;
import com.kivislime.filestorage.dto.FileInfoResponse;
import com.kivislime.filestorage.dto.FileUploadCommand;
import com.kivislime.filestorage.exception.FileUploadException;
import com.kivislime.filestorage.security.UserPrincipal;
import com.kivislime.filestorage.service.FileService;
import com.kivislime.filestorage.validation.ValidDirectoryPath;
import com.kivislime.filestorage.validation.ValidResourcePath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
@RestController
@RequestMapping("/resource")
public class FileController {
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<FileInfoResponse> resourceInfo(@RequestParam @ValidResourcePath String path,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        FileInfoResponse file = fileService.getResourceInfo(principal.getId(), path);
        return ResponseEntity.ok(file);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileInfoResponse>> searchFiles(@RequestParam("query") @ValidResourcePath String path,
                                                              @AuthenticationPrincipal UserPrincipal principal) {
        List<FileInfoResponse> fileList = fileService.listResourcesRecursive(principal.getId(), path);
        return ResponseEntity.ok(fileList);
    }

    @GetMapping("/move")
    public ResponseEntity<List<FileInfoResponse>> moveFile(@RequestParam @ValidResourcePath String from,
                                                           @RequestParam @ValidResourcePath String to,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        List<FileInfoResponse> fileList = fileService.moveResource(principal.getId(), from, to);
        return ResponseEntity.ok(fileList);
    }

    @GetMapping("download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam @ValidResourcePath String path,
                                                            @AuthenticationPrincipal UserPrincipal principal) {

        FileDownloadResult file = fileService.downloadResource(principal.getId(), path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(file.stream());
    }

    @PostMapping
    public ResponseEntity<List<FileInfoResponse>> uploadFile(@RequestParam @ValidDirectoryPath String path,
                                                             @RequestParam("object") List<MultipartFile> file,
                                                             @AuthenticationPrincipal UserPrincipal principal) {
        List<FileUploadCommand> fileUploads = file.stream()
                .map(f -> new FileUploadCommand(
                        f.getOriginalFilename(),
                        f.getContentType(),
                        f.getSize(),
                        multipartFileSupplier(f, principal.getId())))
                .toList();

        List<FileInfoResponse> fileInfoResponseList = fileService.uploadResourceList(principal.getId(), path, fileUploads);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fileInfoResponseList);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFile(@RequestParam @ValidResourcePath String path,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        fileService.deleteResource(principal.getId(), path);
        return ResponseEntity.noContent().build();
    }

    private Supplier<InputStream> multipartFileSupplier(MultipartFile file, Long userId) {
        return () -> {
            try {
                return file.getInputStream();
            } catch (IOException e) {
                throw new FileUploadException("Failed to read uploaded file for userId=" + userId, e);
            }
        };
    }

}
