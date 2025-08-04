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
    public ResponseEntity<FileInfoDto> getFileInfo(@RequestParam String path,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        FileInfoDto file = fileService.getFileInfo(principal.getId(), path);
        return new ResponseEntity<>(file, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileInfoDto>> searchFiles(@RequestParam String path,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        List<FileInfoDto> fileList = fileService.getFileList(principal.getId(), path);
        return new ResponseEntity<>(fileList, HttpStatus.OK);
    }

    @GetMapping("/move")
    public ResponseEntity<FileInfoDto> moveFile(@RequestParam String fromKey,
                                                @RequestParam String toKey,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        FileInfoDto fileList;
        if (FileParserUtil.getNameFromPath(fromKey).equals(FileParserUtil.getNameFromPath(toKey))) {
            fileList = fileService.moveFile(principal.getId(), fromKey, toKey);
        } else {
            fileList = fileService.renameFile(principal.getId(), fromKey, toKey);
        }

        return new ResponseEntity<>(fileList, HttpStatus.OK);
    }

    @GetMapping("download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam String path,
                                                            @AuthenticationPrincipal UserPrincipal principal) {
        //TODO: вернуться к этой идее после того как подниму фронтенд
        // PresignedUrlDto url = fileService.downloadFileByUrl(principal.getId(), path);
        // return new ResponseEntity<>(url, HttpStatus.OK);
        FileDownloadDto file = fileService.download(principal.getId(), path);
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
    public ResponseEntity<List<FileInfoDto>> uploadFile(@RequestParam String path,
                                                        @RequestParam MultipartFile file,
                                                        @AuthenticationPrincipal UserPrincipal principal) throws IOException {
        FileUploadRequest fileUploadRequest = new FileUploadRequest(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getBytes()); //TODO: маппер?
        List<FileInfoDto> fileInfoDtoList = fileService.uploadFile(principal.getId(), path, fileUploadRequest);
        return new ResponseEntity<>(fileInfoDtoList, HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFile(@RequestParam String path,
                                           @AuthenticationPrincipal UserPrincipal principal) {
        fileService.deleteFile(principal.getId(), path);
        return ResponseEntity.noContent().build();
    }

}
