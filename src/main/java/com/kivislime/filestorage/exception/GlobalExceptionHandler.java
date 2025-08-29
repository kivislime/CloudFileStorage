package com.kivislime.filestorage.exception;

import com.kivislime.filestorage.infra.ObjectStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String INTERNAL_ERROR_MESSAGE = "Oops, something went wrong";
    private final MultipartProperties multipartProperties;
    private final ObjectStorageProperties objectStorageProperties;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Validation error ", ex);
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("."));

        return buildResponse(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, String>> handleParamValidation(HandlerMethodValidationException ex) {
        log.error("Invalid path ");
        return buildResponse("Invalid path", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserFileNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserFileNotFoundException ex) {
        log.error(ex.getMessage());
        return buildResponse("User file not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.error(ex.getMessage());
        return buildResponse("User already exists", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleFileNotFoundException(FileNotFoundException ex) {
        log.error(ex.getMessage());
        return buildResponse("File not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleFileAlreadyExistsException(FileAlreadyExistsException ex) {
        log.error(ex.getMessage());
        return buildResponse("File(s) already exists", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidResourceMoveException.class)
    public ResponseEntity<Map<String, String>> handleInvalidResourceMoveException(InvalidResourceMoveException ex) {
        log.error(ex.getMessage());
        return buildResponse("Invalid path to move/rename a resource", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DirectoryAlreadyExists.class)
    public ResponseEntity<Map<String, String>> handleDirectoryAlreadyExists(DirectoryAlreadyExists ex) {
        log.error(ex.getMessage());
        return buildResponse("Directory already exists", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DirectoryNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleDirectoryNotFoundException(DirectoryNotFoundException ex) {
        log.error(ex.getMessage());
        return buildResponse("Directory not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ObjectStorageException.class)
    public ResponseEntity<Map<String, String>> handleObjectStorageException(ObjectStorageException ex) {
        log.error(ex.getMessage());
        return buildResponse(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MinioInitializerException.class)
    public ResponseEntity<Map<String, String>> handleMinioInitializerException(MinioInitializerException ex) {
        log.error(ex.getMessage());
        return buildResponse(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidMimeTypeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidMimeTypeException(InvalidMimeTypeException ex) {
        log.error(ex.getMessage());
        return buildResponse(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidFileExtensionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFileExtensionException(InvalidFileExtensionException ex) {
        log.error(ex.getMessage());
        return buildResponse("Not allowed extension. Allowed extensions: " + objectStorageProperties.allowedExtensions(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<Map<String, String>> handleFileUploadException(FileUploadException ex) {
        log.error(ex.getMessage());
        return buildResponse(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ZipCreateArchiveHierarchyException.class)
    public ResponseEntity<Map<String, String>> handleZipCreateArchiveHierarchyException(ZipCreateArchiveHierarchyException ex) {
        log.error(ex.getMessage());
        return buildResponse(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error(ex.getMessage());
        return buildResponse("You have exceeded the maximum file size: " + multipartProperties.getMaxRequestSize(), HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        log.error("Unhandled exception ", ex);
        return buildResponse(INTERNAL_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, String>> buildResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", message));
    }

}
