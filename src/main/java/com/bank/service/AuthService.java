package com.bank.service;

import com.bank.dto.DTORequest;
import com.bank.entity.User;
import com.bank.util.JwtUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class AuthService {

    @Inject
    UserService userService;

    // Login user and return auth response with token
    public DTORequest.AuthResponse login(DTORequest.LoginRequest request) {
        Optional<User> user = userService.authenticateUser(request.getUsername(), request.getPassword());

        if (user.isPresent()) {
            User foundUser = user.get();
            String token = JwtUtil.generateToken(foundUser.id, foundUser.getUsername());
            return new DTORequest.AuthResponse(token, "Login successful", foundUser.id);
        }
        throw new IllegalArgumentException("Invalid username or password");
    }

    // Register user
    public DTORequest.AuthResponse register(DTORequest.RegisterRequest request) {
        User newUser = userService.registerUser(request);
        String token = JwtUtil.generateToken(newUser.id, newUser.getUsername());
        return new DTORequest.AuthResponse(token, "Registration successful", newUser.id);
    }
}
