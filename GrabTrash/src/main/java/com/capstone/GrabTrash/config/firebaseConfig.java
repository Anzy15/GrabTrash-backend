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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class firebaseConfig {

    private static Firestore firestoreInstance; // Store Firestore instance to prevent closing

    @Bean
    public Firestore firestore() throws IOException {
        if (firestoreInstance == null) {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount;
                
                // Try to get service account from environment variable first
                String serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
                if (serviceAccountJson != null && !serviceAccountJson.isEmpty()) {
                    serviceAccount = new ByteArrayInputStream(serviceAccountJson.getBytes());
                } else {
                    // Fallback to file in resources (for local development)
                    serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");
                    if (serviceAccount == null) {
                        throw new IOException("Service Account Key not found! Set FIREBASE_SERVICE_ACCOUNT_JSON environment variable or place serviceAccountKey.json in resources!");
                    }
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
            }
            firestoreInstance = FirestoreClient.getFirestore();
        }
        return firestoreInstance; // Always return the same Firestore instance
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }
    
    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }
}

