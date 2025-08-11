package com.kivislime.filestorage;

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
    public ResponseEntity<FileInfoDto> resourceInfo(@RequestParam @ValidResourcePath String path,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        FileInfoDto file = fileService.getResourceInfo(principal.getId(), path);
        return new ResponseEntity<>(file, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileInfoDto>> searchFiles(@RequestParam @ValidDirectoryPath String path,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        List<FileInfoDto> fileList = fileService.listResourcesRecursive(principal.getId(), path);
        return new ResponseEntity<>(fileList, HttpStatus.OK);
    }

    @GetMapping("/move")
    public ResponseEntity<FileInfoDto> moveFile(@RequestParam @ValidResourcePath String fromKey,
                                                @RequestParam @ValidResourcePath String toKey,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        FileInfoDto fileList = fileService.moveResource(principal.getId(), fromKey, toKey);
        return new ResponseEntity<>(fileList, HttpStatus.OK);
    }

    @GetMapping("download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam @ValidResourcePath String path,
                                                            @AuthenticationPrincipal UserPrincipal principal) {
        //TODO: вернуться к этой идее после того как подниму фронтенд
        // PresignedUrlDto url = fileService.downloadFileByUrl(principal.getId(), path);
        // return new ResponseEntity<>(url, HttpStatus.OK);
        FileDownloadDto file = fileService.downloadResource(principal.getId(), path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.contentLength())
                .body(file.stream());
    }

    //TODO: какой будет exception при файлу размером больше 10 кб? чек
    //  throws IOException? Оставить?
    // везде пишу AuthenticationPrincipal? Облегчить?
    //TODO: почему работает без указания в скобках параметра? RequestParam
    @PostMapping
    public ResponseEntity<List<FileInfoDto>> uploadFile(@RequestParam @ValidDirectoryPath String path,
                                                        @RequestParam MultipartFile file,
                                                        @AuthenticationPrincipal UserPrincipal principal) throws IOException {
        FileUploadRequest fileUploadRequest = new FileUploadRequest(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream()); //TODO: маппер?
        List<FileInfoDto> fileInfoDtoList = fileService.uploadResource(principal.getId(), path, fileUploadRequest);
        return new ResponseEntity<>(fileInfoDtoList, HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFile(@RequestParam @ValidResourcePath String path,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        fileService.deleteResource(principal.getId(), path);
        return ResponseEntity.noContent().build();
    }

}
