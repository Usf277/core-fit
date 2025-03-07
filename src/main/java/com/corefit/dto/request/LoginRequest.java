package com.corefit.dto.request;

import com.corefit.enums.UserType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String email;
    private String password;
    private UserType type;
}
