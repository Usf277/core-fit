package com.corefit.service.helper;

import com.corefit.dto.request.ContactMessageRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.helper.ContactMessage;
import com.corefit.repository.helper.ContactMessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ContactMessageService {

    @Autowired
    private ContactMessageRepo contactMessageRepo;

    @Transactional
    public GeneralResponse<?> submitMessage(ContactMessageRequest request) {
        ContactMessage message = ContactMessage.builder()
                .name(request.getName())
                .email(request.getEmail())
                .subject(request.getSubject())
                .message(request.getMessage())
                .submittedAt(LocalDateTime.now())
                .build();

        contactMessageRepo.save(message);
        return new GeneralResponse<>("Your message has been submitted successfully");
    }
}
