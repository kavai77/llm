package com.himadri.llm.db;

import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.himadri.llm.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DatabaseService {
    private final Firestore firestore;
    private final AuthenticationService authenticationService;

    public void addLogin() {
        var firebaseToken = authenticationService.getFirebaseToken();
        var userDocument = firestore.collection("users").document(firebaseToken.getUid());
        userDocument.create(Map.of(
            "email", firebaseToken.getEmail(),
            "name", firebaseToken.getName(),
            "inferenceNumber", 0,
            "registrationDate", FieldValue.serverTimestamp(),
            "lastLogin",FieldValue.serverTimestamp()
        ));
        userDocument.update("lastLogin", FieldValue.serverTimestamp());
    }

    public void addInference(String userId, String request, String response) {
        firestore.collection("users").document(userId).update("inferenceNumber", FieldValue.increment(1));
        firestore.collection("inference").add(Map.of(
            "userId", userId,
            "request", request,
            "response", response,
            "date", FieldValue.serverTimestamp()
        ));
    }
}
