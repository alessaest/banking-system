package com.bank.service;

import com.bank.dto.DTORequest;
import com.bank.entity.Account;
import com.bank.entity.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mindrot.jbcrypt.BCrypt.hashpw;
import static org.mindrot.jbcrypt.BCrypt.gensalt;

// ─────────────────────────────────────────────────────────────────────────────
// UserServiceTest
// ─────────────────────────────────────────────────────────────────────────────

@QuarkusTest
class UserServiceTest extends BaseServiceTest {

    @Inject
    UserService userService;

    @Inject
    UserRepository userRepository;

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountService accountService;

    // ─── Helper Methods ────────────────────────────────────────────────

    private DTORequest.RegisterRequest makeValidRequest() {
        DTORequest.RegisterRequest req = new DTORequest.RegisterRequest();
        req.setUsername("johndoe");
        req.setPassword("secret123");
        req.setEmail("john@example.com");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setAccountType("DEBIT");
        req.setInitialDebitBalance(0.0);
        return req;
    }

    // ─── Register Tests ────────────────────────────────────────────────

    @Nested
    @DisplayName("registerUser()")
    class RegisterTests {

        @Test
        @DisplayName("Valid registration request creates and persists user")
//        @Transactional
        void register_success() {
            DTORequest.RegisterRequest req = makeValidRequest();

            User result = userService.registerUser(req);

            assertNotNull(result);
            assertEquals("johndoe", result.getUsername());
            assertEquals("user", result.getRole());

            // Verify user was persisted in database
            Optional<User> dbUser = userRepository.findByUsername("johndoe");
            assertTrue(dbUser.isPresent());
            assertEquals("john@example.com", dbUser.get().getEmail());
        }

        @Test
        @DisplayName("Duplicate username throws IllegalArgumentException")
//        @Transactional
        void register_duplicate_username_throws() {
            DTORequest.RegisterRequest req1 = makeValidRequest();
            DTORequest.RegisterRequest req2 = makeValidRequest();
            req2.setEmail("different@example.com");

            // Register first user
            userService.registerUser(req1);

            // Try to register with same username
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req2));
            assertTrue(ex.getMessage().contains("johndoe"));
        }

        @Test
        @DisplayName("Duplicate email throws IllegalArgumentException")
//        @Transactional
        void register_duplicate_email_throws() {
            DTORequest.RegisterRequest req1 = makeValidRequest();
            DTORequest.RegisterRequest req2 = makeValidRequest();
            req2.setUsername("janedoe");

            // Register first user
            userService.registerUser(req1);

            // Try to register with same email
            assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req2));
        }

        @Test
        @DisplayName("Blank username throws IllegalArgumentException")
        void register_blank_username_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            req.setUsername("  ");

            assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req));
        }

        @Test
        @DisplayName("Password shorter than 6 chars throws IllegalArgumentException")
        void register_short_password_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            req.setPassword("abc");

            assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req));
        }

        @Test
        @DisplayName("Invalid email (no @) throws IllegalArgumentException")
        void register_invalid_email_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            req.setEmail("invalidemail");

            assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req));
        }

        @Test
        @DisplayName("Blank firstName throws IllegalArgumentException")
        void register_blank_firstname_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            req.setFirstName("");

            assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req));
        }

        @Test
        @DisplayName("Blank lastName throws IllegalArgumentException")
        void register_blank_lastname_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            req.setLastName("");

            assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req));
        }

        @Test
        @DisplayName("Invalid account type throws IllegalArgumentException")
        void register_invalid_account_type_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            req.setAccountType("SAVINGS");

            assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req));
        }

        @Test
        @DisplayName("BOTH type with negative initial balance throws IllegalArgumentException")
        void register_both_negative_balance_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            req.setAccountType("BOTH");
            req.setInitialDebitBalance(-10.0);

            assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req));
        }
    }

    // ─── Authenticate Tests ────────────────────────────────────────────

    @Nested
    @DisplayName("authenticateUser()")
    class AuthenticateTests {

        @Test
        @DisplayName("Returns empty Optional for non-existent username")
        void authenticate_unknown_username_returns_empty() {
            Optional<User> result = userService.authenticateUser("ghost", "pass");
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Returns empty Optional for wrong password")
//        @Transactional
        void authenticate_wrong_password_returns_empty() {
            // Create a user with hashed password
            DTORequest.RegisterRequest req = makeValidRequest();
            User registeredUser = userService.registerUser(req);

            // Try to authenticate with wrong password
            Optional<User> result = userService.authenticateUser("johndoe", "wrongpass");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Returns user Optional for correct credentials")
//        @Transactional
        void authenticate_correct_credentials_returns_user() {
            // Register user
            DTORequest.RegisterRequest req = makeValidRequest();
            User registeredUser = userService.registerUser(req);

            // Authenticate with correct password
            Optional<User> result = userService.authenticateUser("johndoe", "secret123");

            assertTrue(result.isPresent());
            assertEquals("johndoe", result.get().getUsername());
        }
    }

    // ─── User Response Tests ────────────────────────────────────────────

    @Nested
    @DisplayName("toUserResponse()")
    class ToUserResponseTests {

        @Test
        @DisplayName("Maps User fields and attached accounts correctly")
//        @Transactional
        void toUserResponse_mapping() {
            // Create user and account
            DTORequest.RegisterRequest req = makeValidRequest();
            User user = userService.registerUser(req);

            DTORequest.UserResponse resp = userService.toUserResponse(user);

            assertEquals(user.id, resp.getId());
            assertEquals("johndoe", resp.getUsername());
            assertNotNull(resp.getAccounts());
        }

        @Test
        @DisplayName("User with no accounts maps to empty accounts list")
//        @Transactional
        void toUserResponse_no_accounts() {
            DTORequest.RegisterRequest req = makeValidRequest();
            User user = userService.registerUser(req);

            user.setAccounts(new ArrayList<>());

            DTORequest.UserResponse resp = userService.toUserResponse(user);

            assertNotNull(resp.getAccounts());
            assertTrue(resp.getAccounts().isEmpty());
        }
    }

    // ─── Get User Tests ────────────────────────────────────────────────

    @Nested
    @DisplayName("getUserById() / getAllUsers()")
    class GetUserTests {

        @Test
        @DisplayName("getUserById returns user when found")
//        @Transactional
        void getById_found() {
            DTORequest.RegisterRequest req = makeValidRequest();
            User registeredUser = userService.registerUser(req);

            Optional<User> result = userService.getUserById(registeredUser.id);
            assertTrue(result.isPresent());
            assertEquals("johndoe", result.get().getUsername());
        }

        @Test
        @DisplayName("getUserById returns empty when not found")
        void getById_not_found() {
            Optional<User> result = userService.getUserById(99999L);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("getAllUsers returns all users from repository")
//        @Transactional
        void getAllUsers_returns_list() {
            // Register multiple users
            DTORequest.RegisterRequest req1 = makeValidRequest();
            DTORequest.RegisterRequest req2 = makeValidRequest();
            req2.setUsername("user2");
            req2.setEmail("user2@example.com");

            userService.registerUser(req1);
            userService.registerUser(req2);

            List<User> result = userService.getAllUsers();
            assertEquals(2, result.size());
        }
    }

    // ─── User Exists Tests ─────────────────────────────────────────────

    @Nested
    @DisplayName("userExists()")
    class UserExistsTests {

        @Test
        @DisplayName("Returns true when user is found")
//        @Transactional
        void userExists_true() {
            DTORequest.RegisterRequest req = makeValidRequest();
            User user = userService.registerUser(req);

            assertTrue(userService.userExists(user.id));
        }

        @Test
        @DisplayName("Returns false when user is not found")
        void userExists_false() {
            assertFalse(userService.userExists(99999L));
        }
    }

    // ─── Delete User Tests ─────────────────────────────────────────────

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUserTests {

        @Test
        @DisplayName("Deletes user successfully")
//        @Transactional
        void deleteUser_success() {
            DTORequest.RegisterRequest req = makeValidRequest();
            User user = userService.registerUser(req);

            userService.deleteUser(user.id);

            Optional<User> result = userRepository.findByIdOptional(user.id);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Deleting non-existent user throws IllegalArgumentException")
        void deleteUser_not_found_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> userService.deleteUser(99999L));
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AuthServiceTest
// ─────────────────────────────────────────────────────────────────────────────

@QuarkusTest
class AuthServiceTest extends BaseServiceTest {

    @Inject
    AuthService authService;

    @Inject
    UserService userService;

    @Inject
    AccountService accountService;

    @Inject
    UserRepository userRepository;

    @Inject
    AccountRepository accountRepository;

    // ─── Helper Methods ────────────────────────────────────────────────

    private DTORequest.RegisterRequest makeValidRequest() {
        DTORequest.RegisterRequest req = new DTORequest.RegisterRequest();
        req.setUsername("johndoe");
        req.setPassword("secret123");
        req.setEmail("john@example.com");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setAccountType("DEBIT");
        req.setInitialDebitBalance(0.0);
        return req;
    }

    // ─── Login Tests ────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Valid credentials return AuthResponse with token and mapped accounts")
//        @Transactional
        void login_success() {
            // Register user first
            DTORequest.RegisterRequest req = makeValidRequest();
            userService.registerUser(req);

            // Login
            DTORequest.LoginRequest loginReq = new DTORequest.LoginRequest("johndoe", "secret123");
            DTORequest.AuthResponse resp = authService.login(loginReq);

            assertNotNull(resp);
            assertEquals("Login successful", resp.getMessage());
            assertNotNull(resp.getUserId());
            assertNotNull(resp.getToken());
            assertFalse(resp.getToken().isEmpty());
        }

        @Test
        @DisplayName("Invalid credentials throw IllegalArgumentException")
        void login_invalid_credentials_throws() {
            DTORequest.LoginRequest req = new DTORequest.LoginRequest("johndoe", "wrong");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> authService.login(req));
            assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
        }
    }

    // ─── Register Tests ────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Successful registration returns AuthResponse with accounts")
//        @Transactional
        void register_success() {
            DTORequest.RegisterRequest req = makeValidRequest();

            DTORequest.AuthResponse resp = authService.register(req);

            assertNotNull(resp);
            assertEquals("Registration successful", resp.getMessage());
            assertNotNull(resp.getUserId());
            assertNotNull(resp.getToken());
        }

        @Test
        @DisplayName("Registration with duplicate username propagates IllegalArgumentException")
//        @Transactional
        void register_duplicate_propagates() {
            DTORequest.RegisterRequest req = makeValidRequest();

            // Register first user
            authService.register(req);

            // Try to register again with same username
            assertThrows(IllegalArgumentException.class,
                    () -> authService.register(req));
        }

        @Test
        @DisplayName("Password is hashed and not stored in plain text")
//        @Transactional
        void register_password_hashed() {
            DTORequest.RegisterRequest req = makeValidRequest();
            String plainPassword = req.getPassword();

            authService.register(req);

            Optional<User> user = userRepository.findByUsername("johndoe");
            assertTrue(user.isPresent());

            String storedPassword = user.get().getPassword();
            assertNotNull(storedPassword);
            assertNotEquals(plainPassword, storedPassword,
                    "Password should be hashed, not stored as plain text");
        }

        @Test
        @DisplayName("Null accountType throws IllegalArgumentException")
        void register_null_account_type_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            req.setAccountType(null);

            assertThrows(IllegalArgumentException.class,
                    () -> authService.register(req));
        }

        @Test
        @DisplayName("Null initialDebitBalance with DEBIT type throws IllegalArgumentException")
        void register_debit_null_balance_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            req.setAccountType("DEBIT");
            req.setInitialDebitBalance(null);

            assertThrows(IllegalArgumentException.class,
                    () -> authService.register(req));
        }

        @Test
        @DisplayName("Negative initialDebitBalance with BOTH type throws IllegalArgumentException")
        void register_both_negative_balance_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            req.setAccountType("BOTH");
            req.setInitialDebitBalance(-50.0);

            assertThrows(IllegalArgumentException.class,
                    () -> authService.register(req));
        }
    }
}
