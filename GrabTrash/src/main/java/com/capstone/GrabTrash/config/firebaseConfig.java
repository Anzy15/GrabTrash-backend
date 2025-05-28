package com.capstone.GrabTrash.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class firebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(firebaseConfig.class);
    private static Firestore firestoreInstance; // Store Firestore instance to prevent closing
    private static FirebaseApp firebaseApp;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (firebaseApp == null) {
            if (FirebaseApp.getApps().isEmpty()) {
                log.info("Initializing Firebase application");
                InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");

                if (serviceAccount == null) {
                    log.error("Service Account Key file not found in resources!");
                    throw new IOException("Service Account Key file not found in resources!");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                firebaseApp = FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            } else {
                log.info("Using existing Firebase application");
                firebaseApp = FirebaseApp.getInstance();
            }
        }
        return firebaseApp;
    }

    @Bean
    public Firestore firestore() throws IOException {
        if (firestoreInstance == null) {
            log.info("Initializing Firestore");
            firestoreInstance = FirestoreClient.getFirestore(firebaseApp());
            log.info("Firestore has been initialized");
        }
        return firestoreInstance; // Always return the same Firestore instance
    }

    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        return FirebaseAuth.getInstance(firebaseApp());
    }
    
    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        log.info("Initializing Firebase Messaging");
        FirebaseMessaging instance = FirebaseMessaging.getInstance(firebaseApp());
        log.info("Firebase Messaging has been initialized");
        return instance;
    }
}

