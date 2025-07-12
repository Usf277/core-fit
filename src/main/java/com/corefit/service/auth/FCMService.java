package com.corefit.service.auth;

import com.corefit.exceptions.GeneralException;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FCMService {

    public void sendNotification(String title, String body, String token) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new GeneralException("❌ FCM failed: " + e.getMessage());
        }
    }

}
