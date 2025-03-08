package com.corefit.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class RegisterRequest {
    private long id;
    private String username;
    private String email;
    private String phone;
    private String password;
    private String gender;
    private String birthDate;
    private Long cityId;
    private String type;
    private String otp;
    private MultipartFile image;
}
