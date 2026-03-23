package com.bank.service;

import com.bank.dto.DTORequest;
import com.bank.entity.Account;
import com.bank.entity.User;
import com.bank.util.JwtUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

//applies the business rules for authentication such as login and registration
@ApplicationScoped
public class AuthService {

    @Inject
    UserService userService;
    @Inject
    AccountService accountService;

    // Login user and return auth response with token
    public DTORequest.AuthResponse login(DTORequest.LoginRequest request) {
        Optional<User> user = userService.authenticateUser(request.getUsername(), request.getPassword());

        if (user.isPresent()) {
            User foundUser = user.get();
            String token = JwtUtil.generateToken(foundUser.id, foundUser.getUsername(), foundUser.getRole());

            List<DTORequest.AccountResponse> accounts = accountService
                    .getMyAccounts(foundUser.id)
                    .stream()
                    .map(accountService::toAccountResponse)
                    .toList();

            return new DTORequest.AuthResponse(token, "Login successful", foundUser.id, accounts);
        }
        throw new IllegalArgumentException("Invalid username or password");
    }

    // Register user
    public DTORequest.AuthResponse register(DTORequest.RegisterRequest request) {
        User newUser = userService.registerUser(request);

        List<Account> accounts = accountService.createAccountForUser(
                newUser,
                request.getAccountType(),
                request.getInitialDebitBalance()
        );

        String token = JwtUtil.generateToken(newUser.id, newUser.getUsername(), newUser.getRole());

        List<DTORequest.AccountResponse> accountResponses = accounts.stream()
                .map(accountService::toAccountResponse)
                .toList();

        return new DTORequest.AuthResponse(token, "Registration successful", newUser.id, accountResponses);
    }
}
