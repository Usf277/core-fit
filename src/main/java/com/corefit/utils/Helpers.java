package com.corefit.utils;

import com.corefit.dto.UserDto;
import com.corefit.entity.User;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Helpers {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private JwtUtil jwtUtil;

    public void validateEmailAndPhone(String email, String phone) {
        if (userRepo.existsByEmail(email)) {
            throw new GeneralException("Email already exists.");
        }
        if (userRepo.existsByPhone(phone)) {
            throw new GeneralException("Phone number already exists.");
        }
    }

    public UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getBirthDate(),
                user.getGovernorate().getName(),
                user.getCity().getName(),
                user.getGender(),
                user.getImageUrl()
        );
    }

    public String extractUserIdFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new GeneralException("Missing or invalid Authorization header");
        }
        String userId = jwtUtil.extractUserId(authorizationHeader.substring(7));

        if (userId == null || userId.isBlank()) {
            throw new GeneralException("Invalid or missing user ID in token");
        }
        return userId;
    }
}
