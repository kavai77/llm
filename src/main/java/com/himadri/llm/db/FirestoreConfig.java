package com.himadri.llm.db;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FirestoreConfig {

    private final FirebaseApp firebaseApp;

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
