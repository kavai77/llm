package com.himadri.llm.db;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.himadri.llm.LlmEndpoint;
import com.himadri.llm.security.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Component
@RequiredArgsConstructor
public class DatabaseService {
    private final Firestore firestore;
    private final AuthenticationService authenticationService;

    public void addLogin() {
        var firebaseToken = authenticationService.getFirebaseToken();
        var userDocument = firestore.collection("users").document(firebaseToken.getUid());
        userDocument.create(Map.of(
            "email", defaultString(firebaseToken.getEmail()),
            "name", defaultString(firebaseToken.getName()),
            "inferenceNumber", 0,
            "registrationDate", FieldValue.serverTimestamp(),
            "lastLogin",FieldValue.serverTimestamp()
        ));
        userDocument.update("lastLogin", FieldValue.serverTimestamp());
    }

    public void addInference(String userId, LlmEndpoint.LlmModel model, String request, String response) {
        firestore.collection("users").document(userId).update("inferenceNumber", FieldValue.increment(1));
        firestore.collection("inference").add(Map.of(
            "userId", userId,
            "model", model.name(),
            "request", request,
            "response", response,
            "date", FieldValue.serverTimestamp()
        ));
    }

    @SneakyThrows
    public long getNumberOfInferences(String userId) {
        return firestore.collection("inference")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", Timestamp.ofTimeSecondsAndNanos(Instant.now().minus(1, ChronoUnit.DAYS).getEpochSecond(), 0))
                .count()
                .get()
                .get()
                .getCount();
    }
}
