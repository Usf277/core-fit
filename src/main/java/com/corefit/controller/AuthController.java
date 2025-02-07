package com.corefit.controller;

import com.corefit.dto.ForgetRequest;
import com.corefit.dto.GeneralResponse;
import com.corefit.dto.LoginRequest;
import com.corefit.dto.RegisterRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<GeneralResponse<?>> register(@RequestBody RegisterRequest request) {
        try {
            GeneralResponse<Object> response = authService.register(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<GeneralResponse<?>> login(@RequestBody LoginRequest request) {
        try {
            GeneralResponse<?> response = authService.login(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/forget_password")
    public ResponseEntity<GeneralResponse<?>> forgetPassword(@RequestBody ForgetRequest request) {
        try {
            GeneralResponse<?> response = authService.forgetPassword(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/confirm_register")
    public ResponseEntity<GeneralResponse<?>> confirmRegister(@RequestBody RegisterRequest request) {
        try {
            GeneralResponse<?> response = authService.confirmRegister(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/check_code")
    public ResponseEntity<GeneralResponse<?>> checkCode(@RequestBody ForgetRequest request) {
        try {
            GeneralResponse<?> response = authService.checkCode(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/reset_password")
    public ResponseEntity<GeneralResponse<?>> resetPassword(@RequestBody ForgetRequest request) {
        try {
            GeneralResponse<?> response = authService.resetPassword(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
