package com.bank.service;

import com.bank.dto.DTORequest;
import com.bank.entity.Account;
import com.bank.entity.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// ─────────────────────────────────────────────────────────────────────────────
// UserServiceTest
// ─────────────────────────────────────────────────────────────────────────────

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    AccountRepository accountRepository;

    // ─── Fixtures ────────────────────────────────────────────────────────────

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

    private User makeUser(Long id) {
        User u = new User();
        u.id = id;
        u.setUsername("johndoe");
        u.setEmail("john@example.com");
        u.setFirstName("John");
        u.setLastName("Doe");
        u.setRole("user");
        u.setCreatedAt(LocalDateTime.now());
        return u;
    }

    // ─── Register ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("registerUser()")
    class RegisterTests {

        @Test
        @DisplayName("Valid registration request creates and persists user")
        void register_success() {
            DTORequest.RegisterRequest req = makeValidRequest();
            when(userRepository.usernameExists("johndoe")).thenReturn(false);
            when(userRepository.emailExists("john@example.com")).thenReturn(false);

            // registerUser internally calls userRepository.persist — no return value needed
            User result = userService.registerUser(req);

            assertNotNull(result);
            assertEquals("johndoe", result.getUsername());
            assertEquals("user", result.getRole());
            verify(userRepository).persist(any(User.class));
        }

        @Test
        @DisplayName("Duplicate username throws IllegalArgumentException")
        void register_duplicate_username_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            when(userRepository.usernameExists("johndoe")).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req));
            assertTrue(ex.getMessage().contains("johndoe"));
        }

        @Test
        @DisplayName("Duplicate email throws IllegalArgumentException")
        void register_duplicate_email_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            when(userRepository.usernameExists("johndoe")).thenReturn(false);
            when(userRepository.emailExists("john@example.com")).thenReturn(true);

            assertThrows(IllegalArgumentException.class,
                    () -> userService.registerUser(req));
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
        @DisplayName("CREDIT type with null initial balance throws IllegalArgumentException")
        void register_credit_null_balance_throws() {
            DTORequest.RegisterRequest req = makeValidRequest();
            req.setAccountType("CREDIT");
            req.setInitialDebitBalance(null);

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

    // ─── Authenticate ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("authenticateUser()")
    class AuthenticateTests {

        @Test
        @DisplayName("Returns empty Optional for non-existent username")
        void authenticate_unknown_username_returns_empty() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            Optional<User> result = userService.authenticateUser("ghost", "pass");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Returns empty Optional for wrong password")
        void authenticate_wrong_password_returns_empty() {
            // BCrypt hash for "correctpass"
            User user = makeUser(1L);
            user.setPassword(org.mindrot.jbcrypt.BCrypt.hashpw("correctpass", org.mindrot.jbcrypt.BCrypt.gensalt()));

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));

            Optional<User> result = userService.authenticateUser("johndoe", "wrongpass");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Returns user Optional for correct credentials")
        void authenticate_correct_credentials_returns_user() {
            User user = makeUser(1L);
            String hashed = org.mindrot.jbcrypt.BCrypt.hashpw("secret123", org.mindrot.jbcrypt.BCrypt.gensalt());
            user.setPassword(hashed);

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));

            Optional<User> result = userService.authenticateUser("johndoe", "secret123");

            assertTrue(result.isPresent());
            assertEquals("johndoe", result.get().getUsername());
        }
    }

    // ─── toUserResponse ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("toUserResponse()")
    class ToUserResponseTests {

        @Test
        @DisplayName("Maps User fields and attached accounts correctly")
        void toUserResponse_mapping() {
            User user = makeUser(1L);

            Account acc = new Account();
            acc.id = 10L;
            acc.setAccountType("DEBIT");
            acc.setAccountNumber("ACC001");
            acc.setBalance(500.0);
            acc.setUser(user);
            acc.setCreatedAt(LocalDateTime.now());

            when(accountRepository.findByUserId(1L)).thenReturn(List.of(acc));

            DTORequest.UserResponse resp = userService.toUserResponse(user);

            assertEquals(1L, resp.getId());
            assertEquals("johndoe", resp.getUsername());
            assertEquals(1, resp.getAccounts().size());
            assertEquals("DEBIT", resp.getAccounts().get(0).getAccountType());
        }

        @Test
        @DisplayName("User with no accounts maps to empty accounts list")
        void toUserResponse_no_accounts() {
            User user = makeUser(1L);
            when(accountRepository.findByUserId(1L)).thenReturn(List.of());

            DTORequest.UserResponse resp = userService.toUserResponse(user);

            assertNotNull(resp.getAccounts());
            assertTrue(resp.getAccounts().isEmpty());
        }
    }

    // ─── getUserById / getAllUsers ────────────────────────────────────────────

    @Nested
    @DisplayName("getUserById() / getAllUsers()")
    class GetUserTests {

        @Test
        @DisplayName("getUserById returns user when found")
        void getById_found() {
            User user = makeUser(1L);
            when(userRepository.findByIdOptional(1L)).thenReturn(Optional.of(user));

            Optional<User> result = userService.getUserById(1L);
            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("getUserById returns empty when not found")
        void getById_not_found() {
            when(userRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

            Optional<User> result = userService.getUserById(99L);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("getAllUsers returns all users from repository")
        void getAllUsers_returns_list() {
            User u1 = makeUser(1L);
            User u2 = makeUser(2L);
            when(userRepository.listAll()).thenReturn(List.of(u1, u2));

            List<User> result = userService.getAllUsers();
            assertEquals(2, result.size());
        }
    }

    // ─── userExists ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("userExists()")
    class UserExistsTests {

        @Test
        @DisplayName("Returns true when user is found")
        void userExists_true() {
            when(userRepository.findByIdOptional(1L)).thenReturn(Optional.of(makeUser(1L)));
            assertTrue(userService.userExists(1L));
        }

        @Test
        @DisplayName("Returns false when user is not found")
        void userExists_false() {
            when(userRepository.findByIdOptional(99L)).thenReturn(Optional.empty());
            assertFalse(userService.userExists(99L));
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// AuthServiceTest
// ─────────────────────────────────────────────────────────────────────────────

@QuarkusTest
class AuthServiceTest {

    @Inject
    AuthService authService;

    @InjectMock
    UserService userService;

    @InjectMock
    AccountService accountService;

    // ─── Login ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Valid credentials return AuthResponse with token and accounts")
        void login_success() {
            User user = new User();
            user.id = 1L;
            user.setUsername("johndoe");
            user.setRole("user");

            DTORequest.AccountResponse accountResp = new DTORequest.AccountResponse(
                    10L, 1L, "ACC001", 500.0, "DEBIT", LocalDateTime.now()
            );

            when(userService.authenticateUser("johndoe", "secret"))
                    .thenReturn(Optional.of(user));
            when(accountService.getMyAccounts(1L))
                    .thenReturn(List.of()); // avoid real Account objects
            // toAccountResponse won't be called since accounts list is empty

            DTORequest.LoginRequest req = new DTORequest.LoginRequest("johndoe", "secret");

            DTORequest.AuthResponse resp = authService.login(req);

            assertNotNull(resp);
            assertEquals("Login successful", resp.getMessage());
            assertEquals(1L, resp.getUserId());
        }

        @Test
        @DisplayName("Invalid credentials throw IllegalArgumentException")
        void login_invalid_credentials_throws() {
            when(userService.authenticateUser("johndoe", "wrong"))
                    .thenReturn(Optional.empty());

            DTORequest.LoginRequest req = new DTORequest.LoginRequest("johndoe", "wrong");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> authService.login(req));
            assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
        }
    }

    // ─── Register ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Successful registration returns AuthResponse with accounts")
        void register_success() {
            User user = new User();
            user.id = 2L;
            user.setUsername("newuser");
            user.setRole("user");

            DTORequest.RegisterRequest req = new DTORequest.RegisterRequest();
            req.setUsername("newuser");
            req.setPassword("pass123");
            req.setEmail("new@example.com");
            req.setFirstName("New");
            req.setLastName("User");
            req.setAccountType("DEBIT");
            req.setInitialDebitBalance(0.0);

            when(userService.registerUser(req)).thenReturn(user);
            when(accountService.createAccountForUser(user, "DEBIT", 0.0))
                    .thenReturn(List.of());

            DTORequest.AuthResponse resp = authService.register(req);

            assertNotNull(resp);
            assertEquals("Registration successful", resp.getMessage());
            assertEquals(2L, resp.getUserId());
        }

        @Test
        @DisplayName("Registration with duplicate username propagates IllegalArgumentException")
        void register_duplicate_propagates() {
            DTORequest.RegisterRequest req = new DTORequest.RegisterRequest();
            req.setUsername("johndoe");
            req.setPassword("pass123");
            req.setEmail("john@example.com");
            req.setFirstName("John");
            req.setLastName("Doe");
            req.setAccountType("DEBIT");
            req.setInitialDebitBalance(0.0);

            when(userService.registerUser(req))
                    .thenThrow(new IllegalArgumentException("Username 'johndoe' already exists"));

            assertThrows(IllegalArgumentException.class,
                    () -> authService.register(req));
        }
    }
}