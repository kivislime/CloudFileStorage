package com.kivislime.filestorage.controller;

import com.kivislime.filestorage.dto.FileDownloadResult;
import com.kivislime.filestorage.dto.FileInfoResponse;
import com.kivislime.filestorage.dto.FileUploadCommand;
import com.kivislime.filestorage.security.UserPrincipal;
import com.kivislime.filestorage.service.FileService;
import com.kivislime.filestorage.validation.ValidDirectoryPath;
import com.kivislime.filestorage.validation.ValidResourcePath;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/resource")
public class FileController {
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<FileInfoResponse> resourceInfo(@RequestParam @ValidResourcePath String path,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        FileInfoResponse file = fileService.getResourceInfo(principal.getId(), path);
        return new ResponseEntity<>(file, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileInfoResponse>> searchFiles(@RequestParam @ValidDirectoryPath String path,
                                                              @AuthenticationPrincipal UserPrincipal principal) {
        List<FileInfoResponse> fileList = fileService.listResourcesRecursive(principal.getId(), path);
        return new ResponseEntity<>(fileList, HttpStatus.OK);
    }

    @GetMapping("/move")
    public ResponseEntity<FileInfoResponse> moveFile(@RequestParam @ValidResourcePath String fromKey,
                                                     @RequestParam @ValidResourcePath String toKey,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        FileInfoResponse fileList = fileService.moveResource(principal.getId(), fromKey, toKey);
        return new ResponseEntity<>(fileList, HttpStatus.OK);
    }

    @GetMapping("download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam @ValidResourcePath String path,
                                                            @AuthenticationPrincipal UserPrincipal principal) {
        //TODO: вернуться к этой идее после того как подниму фронтенд
        // PresignedUrlResponse url = fileService.downloadFileByUrl(principal.getId(), path);
        // return new ResponseEntity<>(url, HttpStatus.OK);
        FileDownloadResult file = fileService.downloadResource(principal.getId(), path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.contentLength())
                .body(file.stream());
    }

    //TODO: какой будет exception при файлу размером больше 10 кб? чек
    // везде пишу AuthenticationPrincipal? Облегчить?
    //TODO: почему работает без указания в скобках параметра? RequestParam
    @PostMapping
    public ResponseEntity<List<FileInfoResponse>> uploadFile(@RequestParam @ValidDirectoryPath String path,
                                                             @RequestParam MultipartFile file,
                                                             @AuthenticationPrincipal UserPrincipal principal) {
        try {
            FileUploadCommand fileUploadCommand = new FileUploadCommand(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream());
            List<FileInfoResponse> fileInfoResponseList = fileService.uploadResource(principal.getId(), path, fileUploadCommand);
            return new ResponseEntity<>(fileInfoResponseList, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFile(@RequestParam @ValidResourcePath String path,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        fileService.deleteResource(principal.getId(), path);
        return ResponseEntity.noContent().build();
    }

}
