package com.bank.util;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class JwtUtil {
    public static String generateToken(Long userId, String username) {
        return Jwt.issuer("com.bank")
                .subject(userId.toString())
                .claim("username", username)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .sign();
    }
}
