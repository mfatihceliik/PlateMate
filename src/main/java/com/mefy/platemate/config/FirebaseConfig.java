package com.mefy.platemate.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-path:/app/secrets/serviceAccountKey.json}")
    private String serviceAccountPath;

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(serviceAccountPath)))
                    .build();

            log.info("Firebase Application is initializing from '{}'...", serviceAccountPath);
            return FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            log.error("Firebase initialization error: {}. Make sure the service account file is mounted at '{}'.", e.getMessage(), serviceAccountPath);
            return null;
        }
    }
}
