package com.corefit.dto;

import com.corefit.enums.UserType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgetRequest {
    private String email;
    private String otp;
    private String password;
    private UserType type;
}
