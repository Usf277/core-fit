package com.corefit.controller.auth;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.market.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    private AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<GeneralResponse<?>> getProfile(@RequestParam long id) {
        try {
            GeneralResponse<?> response = authService.getProfile(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GeneralResponse<>(e.getMessage()));
        }
    }
}
