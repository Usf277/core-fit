package com.corefit.service;

import com.corefit.config.JwtUtil;
import com.corefit.dto.*;
import com.corefit.entity.City;
import com.corefit.entity.User;
import com.corefit.enums.Gender;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.UserRepo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepo userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final GovernorateService governorateService;
    private final CityService cityService;
    private final EmailService emailService;
    private final OtpService otpService;

    public AuthService(UserRepo userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, GovernorateService governorateService, CityService cityService, EmailService emailService, OtpService otpService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.governorateService = governorateService;
        this.cityService = cityService;
        this.emailService = emailService;
        this.otpService = otpService;
    }

    public GeneralResponse<Object> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new GeneralException("Email already exists.");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new GeneralException("Phone number already exists.");
        }

        String otp = otpService.generateOtp(request.getEmail());
        emailService.sendOtpEmail(request.getEmail(), otp);
        return new GeneralResponse<>("OTP sent successfully, please check your email");
    }

    public GeneralResponse<?> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getId());

                    Map<String, Object> data = new HashMap<>();

                    UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getPhone()
                            , user.getBirthDate(), user.getGovernorate().getName(), user.getCity().getName()
                            , user.getGender());

                    data.put("token", token);
                    data.put("user", userDto);

                    return new GeneralResponse<>("Login Successful", data);
                })
                .orElseThrow(() -> new GeneralException("Invalid Credentials"));
    }

    public GeneralResponse<?> forgetPassword(ForgetRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .map(_ -> {
                    String otp = otpService.generateOtp(request.getEmail());
                    emailService.sendOtpEmail(request.getEmail(), otp);
                    return new GeneralResponse<>("OTP sent successfully, please check your email");
                })
                .orElseThrow(() -> new GeneralException("There is no account related to this email"));
    }

    public GeneralResponse<?> confirmRegister(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new GeneralException("Email already exists.");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new GeneralException("Phone number already exists.");
        }
        if (otpService.validateOtp(request.getEmail(), request.getOtp())) {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhone(request.getPhone());
            user.setGender(Gender.valueOf(request.getGender()));
            user.setBirthDate(request.getBirthDate());
            City city = cityService.findById(request.getCityId());
            user.setCity(city);
            user.setGovernorate(governorateService.findById(city.getGovernorate().getId()));
            user.setType(UserType.valueOf(request.getType()));

            userRepository.save(user);
            return new GeneralResponse<>("User registered successfully!");

        } else {
            throw new GeneralException("Invalid OTP");
        }
    }
}
