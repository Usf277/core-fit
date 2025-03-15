package com.corefit.dto.request.market;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CategoryRequest {
    private long id;
    private String name;
    private MultipartFile image;
}
