package com.corefit.dto;

import com.corefit.enums.Gender;
import com.corefit.enums.UserType;
import java.time.LocalDate;


public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String governorate;
    private String city;
    private Gender gender;
    private UserType type;

    public UserDto(Long id, String username, String email, String phone, LocalDate  birthDate, String governorate, String city, Gender gender, UserType type) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.governorate = governorate;
        this.city = city;
        this.gender = gender;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate  getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate  birthDate) {
        this.birthDate = birthDate;
    }

    public String getGovernorate() {
        return governorate;
    }

    public void setGovernorate(String governorate) {
        this.governorate = governorate;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public UserType getType() {
        return type;
    }

    public void setType(UserType type) {
        this.type = type;
    }
}
