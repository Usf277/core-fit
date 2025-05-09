package com.corefit.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.config:}")
    private String firebaseConfig;

    @PostConstruct
    public void initFirebase() {
        try {
            if (firebaseConfig != null && !firebaseConfig.isBlank()) {
                String processedConfig = firebaseConfig.replace("\\n", "\n");

                InputStream serviceAccount = new ByteArrayInputStream(processedConfig.getBytes(StandardCharsets.UTF_8));

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    System.out.println("✅ Firebase initialized successfully.");
                }
            } else {
                System.err.println("❌ Firebase configuration is missing.");
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
