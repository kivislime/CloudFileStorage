package com.kivislime.filestorage.controller;

import com.kivislime.filestorage.dto.AuthResponse;
import com.kivislime.filestorage.service.AuthService;
import com.kivislime.filestorage.dto.UserCredentialsRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody UserCredentialsRequest userCredentialsRequest) {
        AuthResponse authResponse = userService.register(userCredentialsRequest);
        log.info("Registered new user: {}", authResponse.username());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authResponse);
    }
}
