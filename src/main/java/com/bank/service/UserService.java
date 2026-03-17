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

        userRepository.persist(user);
        return user;
    }

    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && BCrypt.checkpw(password, userOpt.get().getPassword())) {
            return userOpt;
        }
        return Optional.empty();
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findByIdOptional(userId);
    }


    public Optional<User> getUserById(String userId) {
        try {
            return userRepository.findByIdOptional(Long.valueOf(userId));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID format");
        }
    }


    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    @Transactional
    public User updateUserProfile(Long userId, DTORequest.RegisterRequest request) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));


        // Validate update input
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Check if new email is already in use by another user
            Optional<User> existing = userRepository.findByEmail(request.getEmail());
            if (existing.isPresent() && !existing.get().id.equals(userId)) {
                throw new IllegalArgumentException("Email already in use by another user");
            }
            user.setEmail(request.getEmail());
        }

        userRepository.persist(user);
        return user;
    }


    @Transactional
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (!BCrypt.checkpw(oldPassword, user.getPassword()))
            throw new IllegalArgumentException("Old password is incorrect");
        if (newPassword == null || newPassword.length() < 6)
            throw new IllegalArgumentException("New password must be at least 6 characters");

        user.setPassword(hashPassword(newPassword));
        userRepository.persist(user);
        return user;
    }


    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.findByIdOptional(userId).isPresent())
            throw new IllegalArgumentException("User not found with ID: " + userId);
        userRepository.deleteById(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.listAll();
    }

    public boolean userExists(Long userId) {
        return userRepository.findByIdOptional(userId).isPresent();
    }


    private void validateRegistrationInput(DTORequest.RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
    }

    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
}
