package com.corefit.controller.helper;

import com.corefit.dto.request.ContactMessageRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.service.helper.ContactMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contact")
public class ContactMessageController {

    @Autowired
    private ContactMessageService contactMessageService;

    @PostMapping
    public ResponseEntity<GeneralResponse<?>> submitMessage(@RequestBody ContactMessageRequest request) {
        return ResponseEntity.ok(contactMessageService.submitMessage(request));
    }
}
