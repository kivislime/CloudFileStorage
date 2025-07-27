package com.kivislime.filestorage;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@RequestBody UserCredentialsDto userCredentialsDto) {
        AuthResponse authResponse = userService.register(userCredentialsDto);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

//    @PostMapping("sign-out")
//    public ResponseEntity<AuthResponse> signOut() {
//        //TODO: из куки достать? юзера. UPD: через параметр метода контроллера положить UsernamePasswordAuthenticationToken. Так как он расширяет Authentication
//    }

    //TODO: надо куда-то положить этот эндпоинт тоже, отдельным сделать? RequestMapping мешают положить в другие
    // @GetMapping("user/me")
}
//TODO: Протестироваьт вход логин, написать тест? Пишу без jwt(мб потом написать версию с jwt, прописаьт в задачи, перейти вроде не сложно)