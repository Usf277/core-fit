package com.corefit.controller.helper;

import com.corefit.dto.response.GeneralResponse;
import com.corefit.enums.AppContentType;
import com.corefit.service.helper.AppContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppContentController {
    @Autowired
    private AppContentService appContentService;

    @GetMapping("/app-content")
    public ResponseEntity<GeneralResponse<?>> getContent() {
        return ResponseEntity.ok(appContentService.getAllContent());
    }
}
