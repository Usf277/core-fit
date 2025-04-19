package com.corefit.service.market;

import com.corefit.dto.request.market.ForgetRequest;
import com.corefit.dto.request.LoginRequest;
import com.corefit.dto.request.RegisterRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.dto.response.UserResponse;
import com.corefit.entity.FcmToken;
import com.corefit.repository.FcmTokenRepo;
import com.corefit.service.*;
import com.corefit.utils.DateParser;
import com.corefit.utils.JwtUtil;
import com.corefit.entity.City;
import com.corefit.entity.User;
import com.corefit.enums.Gender;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private FcmTokenRepo fcmTokenRepo;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private GovernorateService governorateService;
    @Autowired
    private CityService cityService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private OtpService otpService;
    @Autowired
    private FilesService filesService;

    public GeneralResponse<?> login(LoginRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .orElseThrow(() -> new GeneralException("Invalid Credentials"));

        if (!user.getType().equals(request.getType())) {
            throw new GeneralException("Type Not Match For This User");
        }

        String token = jwtUtil.generateToken(user.getId());
        Map<String, Object> data = Map.of("token", token, "user", toUserDto(user));

        return new GeneralResponse<>("Login Successful", data);
    }

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
        userRepo.save(user);
        return new GeneralResponse<>("User registered successfully!");
    }

    public GeneralResponse<?> forgetPassword(ForgetRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new GeneralException("There is no account related to this email"));

        if (!user.getType().equals(request.getType())) {
            throw new GeneralException("Type Not Match For This User");
        }

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
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new GeneralException("No account found with this email"));

        if (!otpService.validateOtp(request.getEmail(), request.getOtp())) {
            throw new GeneralException("Invalid or expired OTP");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepo.save(user);
        return new GeneralResponse<>("Password reset successfully");
    }

    public GeneralResponse<?> getProfile(long id) {
        User user = findUserById(id);
        return new GeneralResponse<>("Successfully retrieved profile", toUserDto(user));
    }

    public GeneralResponse<?> editProfile(RegisterRequest request, HttpServletRequest httpRequest) {
        long userId = extractUserIdFromRequest(httpRequest);
        if (userId != request.getId()) {
            throw new GeneralException("Invalid user ID");
        }

        User user = findUserById(request.getId());
        updateUser(request, user);
        userRepo.save(user);
        return new GeneralResponse<>("Profile updated successfully", toUserDto(user));
    }

    public GeneralResponse<?> deleteAccount(HttpServletRequest httpRequest) {
        long userId = extractUserIdFromRequest(httpRequest);
        userRepo.deleteById(userId);
        return new GeneralResponse<>("Account deleted successfully");
    }

    public GeneralResponse<?> saveFcmToken(String token, HttpServletRequest httpRequest) {
        User user = extractUserFromRequest(httpRequest);
        FcmToken fcmToken = fcmTokenRepo.findByUserId(user.getId());

        if (fcmToken == null) {
            FcmToken fcmTokenNew = new FcmToken();

            fcmTokenNew.setUser(user);
            fcmTokenNew.setToken(token);

            fcmTokenRepo.save(fcmTokenNew);
        } else {
            fcmToken.setToken(token);
            fcmTokenRepo.save(fcmToken);
        }

        return new GeneralResponse<>("Token successfully saved");
    }

    /// Helper method
    public User createUser(RegisterRequest request) {
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
                .birthDate(DateParser.parseDate(request.getBirthDate()))
                .city(city)
                .governorate(governorateService.findById(city.getGovernorate().getId()))
                .type(UserType.valueOf(request.getType()))
                .imageUrl(imagePath)
                .build();
    }

    public void updateUser(RegisterRequest request, User user) {
        if (!user.getEmail().equals(request.getEmail()) && userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new GeneralException("This email is already used in another account");
        }

        if (!user.getPhone().equals(request.getPhone()) && userRepo.findByPhone(request.getPhone()).isPresent()) {
            throw new GeneralException("This phone is already used in another account");
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
        user.setBirthDate(DateParser.parseDate(request.getBirthDate()));
        City city = cityService.findById(request.getCityId());
        user.setCity(city);
        user.setImageUrl(imagePath);
        user.setGovernorate(governorateService.findById(city.getGovernorate().getId()));
    }

    public UserResponse toUserDto(User user) {
        return new UserResponse(
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

    public void validateEmailAndPhone(String email, String phone) {
        if (userRepo.existsByEmail(email)) {
            throw new GeneralException("Email already exists.");
        }
        if (userRepo.existsByPhone(phone)) {
            throw new GeneralException("Phone number already exists.");
        }
    }

    public Long extractUserIdFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new GeneralException("Missing or invalid Authorization header");
        }
        String userId = jwtUtil.extractUserId(authorizationHeader.substring(7));

        if (userId == null || userId.isBlank()) {
            throw new GeneralException("Invalid or missing user ID in token");
        }
        return Long.parseLong(userId);
    }

    public User extractUserFromRequest(HttpServletRequest request) {
        long userId = extractUserIdFromRequest(request);
        return userRepo.findById(userId)
                .orElseThrow(() -> new GeneralException("User not found"));

    }

    public User findUserById(long userId) {
        return userRepo.findById(userId).orElseThrow(() -> new GeneralException("User not found"));
    }
}
