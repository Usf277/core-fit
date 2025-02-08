package com.corefit.service;

import com.corefit.config.JwtUtil;
import com.corefit.dto.*;
import com.corefit.entity.City;
import com.corefit.entity.User;
import com.corefit.enums.Gender;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    private final FilesService filesService;

    public AuthService(UserRepo userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, GovernorateService governorateService, CityService cityService, EmailService emailService, OtpService otpService, FilesService filesService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.governorateService = governorateService;
        this.cityService = cityService;
        this.emailService = emailService;
        this.otpService = otpService;
        this.filesService = filesService;
    }

    public GeneralResponse<?> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .orElseThrow(() -> new GeneralException("Invalid Credentials"));

        String token = jwtUtil.generateToken(user.getId());
        Map<String, Object> data = Map.of("token", token, "user", toUserDto(user));

        return new GeneralResponse<>("Login Successful", data);
    }

    // start register methods
    public GeneralResponse<Object> canRegister(RegisterRequest request) {
        validateEmailAndPhone(request.getEmail(), request.getPhone());

        String otp = otpService.generateOtp(request.getEmail());
        emailService.sendOtpEmail(request.getEmail(), otp);
        return new GeneralResponse<>("OTP sent successfully, please check your email");
    }

    public GeneralResponse<?> register(RegisterRequest request) {
        validateEmailAndPhone(request.getEmail(), request.getPhone());

        if (!otpService.validateOtp(request.getEmail(), request.getOtp())) {
            throw new GeneralException("Invalid OTP");
        }

        User user = createUser(request);
        userRepository.save(user);
        return new GeneralResponse<>("User registered successfully!");
    }

    // start reset password methods
    public GeneralResponse<?> forgetPassword(ForgetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GeneralException("There is no account related to this email"));

        String otp = otpService.generateOtp(user.getEmail());
        emailService.sendOtpEmail(user.getEmail(), otp);
        return new GeneralResponse<>("OTP sent successfully, please check your email");
    }

    public GeneralResponse<?> checkCode(ForgetRequest request) {
        if (!otpService.validateOtp(request.getEmail(), request.getOtp())) {
            throw new GeneralException("Invalid or expired OTP");
        }
        return new GeneralResponse<>("OTP Confirmed");
    }

    @Transactional
    public GeneralResponse<?> resetPassword(ForgetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new GeneralException("No account found with this email"));

        if (!otpService.validateOtp(request.getEmail(), request.getOtp())) {
            throw new GeneralException("Invalid or expired OTP");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return new GeneralResponse<>("Password reset successfully");
    }


    public GeneralResponse<?> getProfile(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new GeneralException("No account found with this id"));

        return new GeneralResponse<>("Successfully retrieved profile", toUserDto(user));
    }

    public GeneralResponse<?> editProfile(RegisterRequest request, HttpServletRequest httpRequest) {
        String userId = extractUserIdFromRequest(httpRequest);
        if (!userId.equals(String.valueOf(request.getId()))) {
            throw new GeneralException("Invalid user ID");
        }

        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new GeneralException("No account found with this id"));

        updateUser(request, user);
        userRepository.save(user);

        return new GeneralResponse<>("Profile updated successfully", toUserDto(user));
    }

    // Helper Methods
    private void validateEmailAndPhone(String email, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new GeneralException("Email already exists.");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new GeneralException("Phone number already exists.");
        }
    }

    private User createUser(RegisterRequest request) {
        City city = cityService.findById(request.getCityId());

        String imagePath = null;

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                imagePath = filesService.saveImage(request.getImage());
            } catch (IOException e) {
                throw new GeneralException("Failed to upload image: " + e.getMessage());
            }
        }

        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .gender(Gender.valueOf(request.getGender()))
                .birthDate(request.getBirthDate())
                .city(city)
                .governorate(governorateService.findById(city.getGovernorate().getId()))
                .type(UserType.valueOf(request.getType()))
                .imageUrl(imagePath)
                .build();
    }

    private void updateUser(RegisterRequest request, User user) {
        if (!user.getEmail().equals(request.getEmail()) && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new GeneralException("This email is already used in another account");
        }

        if (!user.getPhone().equals(request.getPhone()) && userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new GeneralException("This phone is already used in another account");
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        String imagePath = user.getImageUrl();

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                if (imagePath != null && !imagePath.isEmpty()) {
                    filesService.deleteImage(imagePath);
                }
                imagePath = filesService.saveImage(request.getImage());
            } catch (IOException e) {
                throw new GeneralException("Failed to upload image: " + e.getMessage());
            }
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(Gender.valueOf(request.getGender()));
        user.setBirthDate(request.getBirthDate());
        City city = cityService.findById(request.getCityId());
        user.setCity(city);
        user.setImageUrl(imagePath);
        user.setGovernorate(governorateService.findById(city.getGovernorate().getId()));
    }

    private UserDto toUserDto(User user) {
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
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new GeneralException("Missing or invalid Authorization header");
        }
        return jwtUtil.extractUserId(authorizationHeader.substring(7));
    }

}
