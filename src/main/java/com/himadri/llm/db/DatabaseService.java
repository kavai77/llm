package com.himadri.llm.db;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.himadri.llm.security.AuthenticationService;
import lombok.Builder;
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

    @SneakyThrows
    public String addInference(String userId, String model, String request, String response) {
        firestore.collection("users").document(userId).update("inferenceNumber", FieldValue.increment(1));
        return firestore.collection("inference").add(Map.of(
            "userId", userId,
            "model", model,
            "request", request,
            "response", response,
            "date", FieldValue.serverTimestamp()
        )).get().getId();
    }

    @SneakyThrows
    public InferenceResponse getInference(String id) {
        var document = firestore.collection("inference").document(id).get().get();
        return InferenceResponse.builder()
                    .request(document.getString("request"))
                    .response(document.getString("response"))
                    .build();
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

    @Builder
    public record InferenceResponse(String request, String response) {}
}
