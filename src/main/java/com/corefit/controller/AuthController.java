package com.corefit.controller;

import com.corefit.dto.ForgetRequest;
import com.corefit.dto.GeneralResponse;
import com.corefit.dto.LoginRequest;
import com.corefit.dto.RegisterRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/can_register")
    public ResponseEntity<GeneralResponse<?>> canRegister(@RequestBody RegisterRequest request) {
        try {
            GeneralResponse<Object> response = authService.canRegister(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping(value = "/register")
    public ResponseEntity<GeneralResponse<?>> register(@RequestBody RegisterRequest request) {
        try {
            GeneralResponse<?> response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    @PostMapping(value = "/edit_profile", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> editProfile(@ModelAttribute RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = authService.editProfile(request, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping(value = "/delete_account")
    public ResponseEntity<GeneralResponse<?>> deleteAccount(HttpServletRequest httpRequest){
        try {
            GeneralResponse<?> response = authService.deleteAccount( httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
