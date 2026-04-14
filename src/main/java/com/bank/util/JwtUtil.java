package com.bank.util;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@ApplicationScoped
public class JwtUtil {
    private JwtUtil() {
        // Private constructor to prevent instantiation
    }

    public static String generateToken(Long userId, String username, String role) {
        return Jwt.issuer("com.bank")
                .subject(userId.toString())
                //.upn(username)
                .claim("username", username)
                .groups(Set.of(role))
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .sign();
    }
}
