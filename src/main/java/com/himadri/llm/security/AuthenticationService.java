package com.himadri.llm.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationService {
    public FirebaseToken parseFirebaseToken(String firebaseToken) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
    }

    public FirebaseToken getFirebaseToken() {
        return (FirebaseToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String getUid() {
        return getFirebaseToken().getUid();
    }
}
