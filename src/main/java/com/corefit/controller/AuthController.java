package com.corefit.controller;

import com.corefit.dto.GeneralResponse;
import com.corefit.dto.LoginRequest;
import com.corefit.dto.RegisterRequest;
import com.corefit.service.AuthService;
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
    public GeneralResponse<String> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public GeneralResponse<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}

