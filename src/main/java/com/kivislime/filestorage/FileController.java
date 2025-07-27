package com.kivislime.filestorage;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
        List<FileInfoDto> fileList = fileService.getFileList(path, principal.getId());
        return new ResponseEntity<>(fileList, HttpStatus.OK);
    }

    //TODO: какой будет exception при файлу размером больше 10 кб? чек
    //  throws IOException? Оставить?
    // везде пишу AuthenticationPrincipal? Облегчить?
    //TODO: почему работает без указания в скобках параметра? RequestParam
    @PostMapping
    public ResponseEntity<List<FileInfoDto>> uploadFile(@RequestParam String path,
                                                        @RequestParam MultipartFile file,
                                                        @AuthenticationPrincipal UserPrincipal principal) throws IOException {
        FileUploadRequest fileUploadRequest = new FileUploadRequest(file.getOriginalFilename(), file.getContentType(), file.getSize(), file.getBytes()); //TODO: маппер?
        List<FileInfoDto> fileInfoDtoList = fileService.uploadFile(principal.getId(), path, fileUploadRequest);
        return new ResponseEntity<>(fileInfoDtoList, HttpStatus.CREATED);
    }

}
