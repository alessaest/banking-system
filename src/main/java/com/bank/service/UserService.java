package com.bank.service;

import com.bank.dto.DTORequest;
import com.bank.entity.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;


//applies the business rules for user management such as registration, authentication, and user retrieval
@ApplicationScoped
public class UserService {


    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    private UserService (UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }


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
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
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

    public DTORequest.UserResponse toUserResponse(User user) {
        List<DTORequest.AccountResponse> accounts = accountRepository.findByUserId(user.id)
                .stream()
                .map(account -> new DTORequest.AccountResponse(
                        account.id,
                        account.getUser().id,
                        account.getAccountNumber(),
                        account.getBalance(),
                        account.getAccountType(),
                        account.getCreatedAt()
                ))
                .toList();

        return new DTORequest.UserResponse(
                user.id,
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                accounts
        );
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findByIdOptional(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.listAll();
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        jakarta.persistence.EntityManager em = userRepository.getEntityManager();

        // delete transactions linked to user's accounts
        em.createNativeQuery(
                "DELETE FROM transaction WHERE from_account_id IN " +
                        "(SELECT id FROM account WHERE user_id = ?1) " +
                        "OR to_account_id IN " +
                        "(SELECT id FROM account WHERE user_id = ?1)"
        ).setParameter(1, userId).executeUpdate();

        // delete accounts
        em.createNativeQuery(
                "DELETE FROM account WHERE user_id = ?1"
        ).setParameter(1, userId).executeUpdate();

        // flush and clear
        em.flush();
        em.clear();

        // delete user
        em.createNativeQuery(
                "DELETE FROM \"user\" WHERE id = ?1"
        ).setParameter(1, userId).executeUpdate();
    }

    public boolean userExists(Long userId) {
        return userRepository.findByIdOptional(userId).isPresent();
    }

    private void validateRegistrationInput(DTORequest.RegisterRequest request) {
        validateCredentials(request.getUsername(), request.getPassword());
        validatePersonalInfo(request.getEmail(), request.getFirstName(), request.getLastName());
        validateAccountType(request.getAccountType(), request.getInitialDebitBalance(), request.getInitialSavingsBalance());
    }

    private void validateCredentials(String username, String password) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be empty");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password must have 6 characters");
    }

    private void validatePersonalInfo(String email, String firstName, String lastName) {
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Invalid email format");
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("First name cannot be empty");
        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("Last name cannot be empty");
    }

    private void validateAccountType(String accountType, Double initialDebitBalance, Double initialSavingsBalance) {
        if (accountType == null || accountType.isBlank())
            throw new IllegalArgumentException("Account type cannot be empty");

        String upperType = accountType.toUpperCase();
        if (!isValidAccountType(upperType))
            throw new IllegalArgumentException("Invalid account type. Use: DEBIT, CREDIT, SAVINGS, DEBIT_CREDIT, DEBIT_SAVINGS, CREDIT_SAVINGS, or ALL");

        validateDebitBalance(upperType, initialDebitBalance);
        validateSavingsBalance(upperType, initialSavingsBalance);
    }

    private boolean isValidAccountType(String type) {
        return type.equals("DEBIT") || type.equals("CREDIT") || type.equals("SAVINGS") ||
                type.equals("DEBIT_CREDIT") || type.equals("DEBIT_SAVINGS") ||
                type.equals("CREDIT_SAVINGS") || type.equals("ALL");
    }

    private void validateDebitBalance(String upperType, Double initialDebitBalance) {
        if (!upperType.equals("CREDIT") &&
                (initialDebitBalance == null || initialDebitBalance < 0)) {
            throw new IllegalArgumentException("Initial debit balance must be non-negative for non-credit accounts");
        }
    }

    private void validateSavingsBalance(String upperType, Double initialSavingsBalance) {
        if ((upperType.contains("SAVINGS") || upperType.equals("ALL")) &&
                (initialSavingsBalance == null || initialSavingsBalance < 0)) {
            throw new IllegalArgumentException("Initial savings balance must be non-negative for savings accounts");
        }
    }

    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
}
