package com.corefit.dto.response;

import com.corefit.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String governorate;
    private String city;
    private Gender gender;
    private String imageUrl;
}
