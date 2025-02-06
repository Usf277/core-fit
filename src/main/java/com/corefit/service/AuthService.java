package com.corefit.service;

import com.corefit.config.JwtUtil;
import com.corefit.dto.GeneralResponse;
import com.corefit.dto.LoginRequest;
import com.corefit.dto.RegisterRequest;
import com.corefit.dto.UserDto;
import com.corefit.entity.City;
import com.corefit.entity.User;
import com.corefit.enums.Gender;
import com.corefit.enums.UserType;
import com.corefit.repository.UserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepo userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final GovernorateService governorateService;
    private final CityService cityService;

    public AuthService(UserRepo userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, GovernorateService governorateService, CityService cityService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.governorateService = governorateService;
        this.cityService = cityService;
    }

    public GeneralResponse<String> register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new GeneralResponse<>("Email already exists!");
        }

        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            return new GeneralResponse<>("Phone already exists!");
        }

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
    }

    public GeneralResponse<?> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getId());

                    Map<String, Object> data = new HashMap<>();

                    UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getPhone()
                            , user.getBirthDate(), user.getGovernorate().getName(), user.getCity().getName()
                            , user.getGender(), user.getType());

                    data.put("token", token);
                    data.put("user", userDto);

                    return new GeneralResponse<>("Login Successful", data);

                })
                .orElse(new GeneralResponse<>("Invalid Credentials"));
    }
}
