package com.himadri.llm.security;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserTokenAuthenticationConverter implements AuthenticationConverter {
    public static final String FIREBASE_AUTH_COOKIE = "FirebaseAuth";
    private final AuthenticationService authenticationService;

    @Override
    public Authentication convert(HttpServletRequest request) {
        try {
            var firebaseCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> FIREBASE_AUTH_COOKIE.equals(cookie.getName()))
                    .findFirst();
            if (firebaseCookie.isEmpty()) {
                throw new AuthenticationCredentialsNotFoundException("Firebase token not found");
            }
            FirebaseToken token = authenticationService.parseFirebaseToken(firebaseCookie.get().getValue());
            return new PreAuthenticatedAuthenticationToken(token, null, List.of(() -> "ROLE_USER"));
        } catch (FirebaseAuthException e) {
            throw new BadCredentialsException(e.getMessage(), e);
        }
    }
}
