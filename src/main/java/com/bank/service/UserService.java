package com.bank.service;

import com.bank.dto.DTORequest;
import com.bank.entity.User;
import com.bank.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Transactional
    public User registerUser(DTORequest.RegisterRequest request) {
        // Validate input
        validateRegistrationInput(request);

        // Check if username already exists
        if (userRepository.usernameExists(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' already exists");
        }

        // Check if email already exists
        if (userRepository.emailExists(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(hashPassword(request.getPassword())); // Password should be encrypted
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole("user");

        userRepository.persist(user);
        return user;
    }

    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && BCrypt.checkpw(password, userOpt.get().getPassword()))
            return userOpt;
        return Optional.empty();
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findByIdOptional(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.listAll();
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.findByIdOptional(userId).isPresent())
            throw new IllegalArgumentException("User not found with id: " + userId);
        userRepository.deleteById(userId);
    }

    public boolean userExists(Long userId) {
        return userRepository.findByIdOptional(userId).isPresent();
    }

    private void validateRegistrationInput(DTORequest.RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank())
            throw new IllegalArgumentException("Username cannot be empty");
        if (request.getPassword() == null || request.getPassword().length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
        if (request.getEmail() == null || !request.getEmail().contains("@"))
            throw new IllegalArgumentException("Invalid email format");
        if (request.getFullName() == null || request.getFullName().isBlank())
            throw new IllegalArgumentException("Full name cannot be empty");
        if (request.getAccountType() == null ||
                (!request.getAccountType().equalsIgnoreCase("DEBIT") &&
                !request.getAccountType().equalsIgnoreCase("CREDIT") &&
                !request.getAccountType().equalsIgnoreCase("BOTH")))
            throw new IllegalArgumentException("Invalid account type");
        if ((request.getAccountType().equalsIgnoreCase("CREDIT") ||
                request.getAccountType().equalsIgnoreCase("BOTH")) &&
            (request.getInitialCreditBalance() == null || request.getInitialCreditBalance() < 0))
            throw new IllegalArgumentException("Initial credit balance must be non-negative for credit accounts");
    }

    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
}
