package com.corefit.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String email;
    private String phone;
    private String password;
    private String gender;
    private String birthDate;
    private String governorate;
    private String city;
    private String type;
}
