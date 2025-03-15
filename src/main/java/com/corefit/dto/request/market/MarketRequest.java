package com.corefit.dto.request.market;


import com.corefit.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class MarketRequest {
    private long id;
    private String name;
    private String description;
    private long categoryId;
    private String lat;
    private String lng;
    private String address;
    private User user;
    private MultipartFile image;
}
