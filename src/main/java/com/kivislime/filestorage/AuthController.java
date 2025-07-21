package com.kivislime.filestorage;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    @PostMapping("sign-in")
    public ResponseEntity<AuthResponse> signIn(@RequestBody UserCredentialsDto userCredentialsDto) {
        AuthResponse authResponse = userService.login(userCredentialsDto);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @PostMapping("sign-up")
    public ResponseEntity<AuthResponse> signUp(@RequestBody UserCredentialsDto userCredentialsDto) {
        AuthResponse authResponse = userService.register(userCredentialsDto);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
        //        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

//    @PostMapping("sign-out")
//    public ResponseEntity<AuthResponse> signOut() {
//        //TODO: из куки достать? юзера
//    }
}
//TODO: РЕАЛИЗОВАТЬ методы post. get - не нужны так как будут лежать на фронте. Пишу без jwt(мб потом написать версию с jwt, прописаьт в задачи, перейти вроде не сложно)