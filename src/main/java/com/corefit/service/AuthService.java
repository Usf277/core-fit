package com.corefit.service;

import com.corefit.config.JwtUtil;
import com.corefit.dto.LoginRequest;
import com.corefit.dto.RegisterRequest;
import com.corefit.entity.User;
import com.corefit.enums.Gender;
import com.corefit.enums.UserType;
import com.corefit.repository.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepo userRepository;
    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepo userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }



    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "Email already exists!";
        }

        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            return "Phone already exists!";
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setGender(Gender.valueOf(request.getGender()));
        user.setBirthDate(request.getBirthDate());
        user.setGovernorate(request.getGovernorate());
        user.setCity(request.getCity());
        user.setType(UserType.valueOf(request.getType()));

        userRepository.save(user);

        return "User registered successfully!";
    }

    public String login(LoginRequest request) {
        System.out.println(request);
        return userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user -> jwtUtil.generateToken(user.getId()))
                .orElse("Invalid Credentials!");
    }
}


