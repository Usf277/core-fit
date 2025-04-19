package com.corefit.controller.auth;

import com.corefit.dto.request.market.ForgetRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.request.LoginRequest;
import com.corefit.dto.request.RegisterRequest;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.market.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/can_register")
    public ResponseEntity<GeneralResponse<?>> canRegister(@RequestBody RegisterRequest request) {
        try {
            GeneralResponse<Object> response = authService.canRegister(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> register(@ModelAttribute RegisterRequest request) {
        try {
            GeneralResponse<?> response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GeneralResponse<>("An unexpected error occurred"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<GeneralResponse<?>> login(@RequestBody LoginRequest request) {
        try {
            GeneralResponse<?> response = authService.login(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/forget_password")
    public ResponseEntity<GeneralResponse<?>> forgetPassword(@RequestBody ForgetRequest request) {
        try {
            GeneralResponse<?> response = authService.forgetPassword(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/check_code")
    public ResponseEntity<GeneralResponse<?>> checkCode(@RequestBody ForgetRequest request) {
        try {
            GeneralResponse<?> response = authService.checkCode(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/reset_password")
    public ResponseEntity<GeneralResponse<?>> resetPassword(@RequestBody ForgetRequest request) {
        try {
            GeneralResponse<?> response = authService.resetPassword(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping(value = "/edit_profile", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneralResponse<?>> editProfile(@ModelAttribute RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = authService.editProfile(request, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @DeleteMapping(value = "/delete_account")
    public ResponseEntity<GeneralResponse<?>> deleteAccount(HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = authService.deleteAccount(httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GeneralResponse<>(e.getMessage()));
        }
    }

    @PostMapping("firebase-token")
    public ResponseEntity<GeneralResponse<?>> saveFcmToken(String fcmToken, HttpServletRequest httpRequest) {
        try {
            GeneralResponse<?> response = authService.saveFcmToken(fcmToken, httpRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
