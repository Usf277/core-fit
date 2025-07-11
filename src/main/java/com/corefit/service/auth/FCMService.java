package com.corefit.service.auth;

import com.google.firebase.messaging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.corefit.repository.auth.FcmTokenRepo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FCMService {
    @Autowired
    private final FcmTokenRepo fcmTokenRepo;

    public void sendNotification(String title, String body, String token) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                    || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                fcmTokenRepo.deleteByToken(token);
            }
            throw new RuntimeException("FCM error: " + e.getMessage());
        }
    }
}
