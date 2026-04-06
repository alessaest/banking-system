package com.bank.service;

import com.bank.dto.DTORequest;
import com.bank.entity.Account;
import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@QuarkusTest
class AccountServiceTest {

    @Inject
    AccountService accountService;

    @InjectMock
    AccountRepository accountRepository;

    @InjectMock
    TransactionRepository transactionRepository;

    @InjectMock
    UserRepository userRepository;

    // test data
    private User makeUser(Long id) {
        User u = new User();
        u.id = id;
        u.setUsername("testuser");
        u.setEmail("test@example.com");
        u.setFirstName("Test");
        u.setLastName("User");
        u.setRole("user");
        u.setCreatedAt(LocalDateTime.now());
        return u;
    }

    private Account makeDebitAccount(Long id, User owner, double balance) {
        Account a = new Account();
        a.id = id;
        a.setAccountNumber("DEBIT123456789");
        a.setBalance(balance);
        a.setAccountType("DEBIT");
        a.setUser(owner);
        return a;
    }

    private Account makeCreditAccount(Long id, User owner, double balance) {
        Account a = new Account();
        a.id = id;
        a.setAccountNumber("CREDIT12345678");
        a.setBalance(balance);
        a.setAccountType("CREDIT");
        a.setUser(owner);
        return a;
    }

    // test cases for deposit methods
    @Nested
    @DisplayName("deposit()")
    class DepositTests {

        @Test
        @DisplayName("Deposit positive amount into owned DEBIT account succeeds")
        void deposit_debit_success() {
            User user = makeUser(1L);
            Account account = makeDebitAccount(10L, user, 500.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(account));

            DTORequest.TransactionResponse result = accountService.depositToDebit(10L, 200.0, 1L);

            assertNotNull(result);
            assertEquals(700.0, account.getBalance(), 0.001);
            assertEquals("DEPOSIT", result.getType());
            assertEquals("Completed", result.getStatus());
            verify(transactionRepository).persist(any(Transaction.class));
        }

        @Test
        @DisplayName("Deposit into owned CREDIT account within credit limit succeeds")
        void deposit_credit_within_limit_success() {
            User user = makeUser(1L);
            Account creditAccount = makeCreditAccount(20L, user, 0.0);
            creditAccount.setCreditLimit(1000.0); // current balance

            when(accountRepository.findByIdOptional(20L)).thenReturn(Optional.of(creditAccount));

            DTORequest.TransactionResponse result = accountService.depositToCredit(20L, 300.0, 1L);

            assertNotNull(result);
            assertEquals(300.0, creditAccount.getBalance(), 0.001);
        }

        @Test
        @DisplayName("Deposit into CREDIT account exceeding fixed credit limit throws exception")
        void deposit_credit_exceeds_limit_throws() {
            User user = makeUser(1L);
            Account creditAccount = makeCreditAccount(20L, user, 800.0); // fixed limit = 1000
            creditAccount.setCreditLimit(1000.0); // already used 800

            when(accountRepository.findByIdOptional(20L)).thenReturn(Optional.of(creditAccount));

            // Trying to deposit 300 when only 200 remaining
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> accountService.depositToCredit(20L, 300.0, 1L)
            );
            assertTrue(ex.getMessage().contains("exceed") || ex.getMessage().contains("limit"),
                    "Should mention credit limit exceeded");
        }

        @Test
        @DisplayName("Deposit zero amount throws IllegalArgumentException")
        void deposit_zero_amount_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToDebit(10L, 0.0, 1L));
        }

        @Test
        @DisplayName("Deposit negative amount throws IllegalArgumentException")
        void deposit_negative_amount_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToDebit(10L, -100.0, 1L));
        }

        @Test
        @DisplayName("Deposit to non-existent account throws IllegalArgumentException")
        void deposit_account_not_found_throws() {
            when(accountRepository.findByIdOptional(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToDebit(99L, 100.0, 1L));
        }

        @Test
        @DisplayName("Deposit to account owned by another user throws IllegalArgumentException")
        void deposit_unauthorized_user_throws() {
            User owner = makeUser(1L);
            Account account = makeDebitAccount(10L, owner, 500.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(account));

            // User 2 tries to deposit into user 1's account
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToDebit(10L, 100.0, 2L));
        }
    }

    // test cases for withdraw methods

    @Nested
    @DisplayName("withdraw()")
    class WithdrawTests {

        @Test
        @DisplayName("Withdraw valid amount from owned account succeeds")
        void withdraw_success() {
            User user = makeUser(1L);
            Account account = makeDebitAccount(10L, user, 500.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(account));

            DTORequest.TransactionResponse result = accountService.withdraw(10L, 200.0, 1L);

            assertNotNull(result);
            assertEquals(300.0, account.getBalance(), 0.001);
            assertEquals("WITHDRAWAL", result.getType());
            verify(transactionRepository).persist(any(Transaction.class));
        }

        @Test
        @DisplayName("Withdraw more than balance throws IllegalArgumentException")
        void withdraw_insufficient_balance_throws() {
            User user = makeUser(1L);
            Account account = makeDebitAccount(10L, user, 100.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(account));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> accountService.withdraw(10L, 500.0, 1L));
            assertTrue(ex.getMessage().toLowerCase().contains("insufficient"));
        }

        @Test
        @DisplayName("Withdraw zero amount throws IllegalArgumentException")
        void withdraw_zero_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.withdraw(10L, 0.0, 1L));
        }

        @Test
        @DisplayName("Withdraw from another user's account throws IllegalArgumentException")
        void withdraw_unauthorized_throws() {
            User owner = makeUser(1L);
            Account account = makeDebitAccount(10L, owner, 500.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(account));

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.withdraw(10L, 100.0, 2L));
        }

        @Test
        @DisplayName("Withdraw from non-existent account throws IllegalArgumentException")
        void withdraw_account_not_found_throws() {
            when(accountRepository.findByIdOptional(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.withdraw(99L, 100.0, 1L));
        }
    }

    // test cases for updateCreditBalance methods

    @Nested
    @DisplayName("updateCreditBalance()")
    class UpdateCreditBalanceTests {

        @Test
        @DisplayName("Admin updates credit account balance successfully")
        void updateCreditBalance_success() {
            User user = makeUser(1L);
            Account creditAccount = makeCreditAccount(20L, user, 500.0);
            creditAccount.setCreditLimit(1000.0);

            when(accountRepository.findByIdOptional(20L)).thenReturn(Optional.of(creditAccount));

            DTORequest.AccountResponse result = accountService.updateCreditBalance(20L, 2000.0);

            assertNotNull(result);
            assertEquals(3000.0, creditAccount.getBalance(), 0.001);
            assertEquals(3000.0, creditAccount.getCreditLimit(), 0.001);
        }

        @Test
        @DisplayName("Updating balance on a DEBIT account throws IllegalArgumentException")
        void updateCreditBalance_on_debit_throws() {
            User user = makeUser(1L);
            Account debitAccount = makeDebitAccount(10L, user, 500.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(debitAccount));

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateCreditBalance(10L, 1000.0));
        }

        @Test
        @DisplayName("Updating with zero balance throws IllegalArgumentException")
        void updateCreditBalance_zero_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateCreditBalance(20L, 0.0));
        }

        @Test
        @DisplayName("Updating with negative balance throws IllegalArgumentException")
        void updateCreditBalance_negative_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateCreditBalance(20L, -500.0));
        }

        @Test
        @DisplayName("Updating non-existent account throws IllegalArgumentException")
        void updateCreditBalance_not_found_throws() {
            when(accountRepository.findByIdOptional(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateCreditBalance(99L, 1000.0));
        }
    }

    // ─── Get Balance ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAccountBalance()")
    class GetBalanceTests {

        @Test
        @DisplayName("Returns correct balance for account owner")
        void getBalance_success() {
            User user = makeUser(1L);
            Account account = makeDebitAccount(10L, user, 750.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(account));

            Double balance = accountService.getAccountBalance(10L, 1L);
            assertEquals(750.0, balance, 0.001);
        }

        @Test
        @DisplayName("Non-owner requesting balance throws IllegalArgumentException")
        void getBalance_unauthorized_throws() {
            User owner = makeUser(1L);
            Account account = makeDebitAccount(10L, owner, 750.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(account));

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.getAccountBalance(10L, 2L));
        }

        @Test
        @DisplayName("Non-existent account throws IllegalArgumentException")
        void getBalance_not_found_throws() {
            when(accountRepository.findByIdOptional(99L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.getAccountBalance(99L, 1L));
        }
    }

    // ─── Create Account ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("createAccountForUser()")
    class CreateAccountTests {

        @Test
        @DisplayName("Creating DEBIT account succeeds when user has no existing DEBIT")
        void createDebit_success() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "DEBIT")).thenReturn(false);

            List<Account> accounts = accountService.createAccountForUser(user, "DEBIT", 500.0);

            assertEquals(1, accounts.size());
            assertEquals("DEBIT", accounts.get(0).getAccountType());
            assertEquals(500.0, accounts.get(0).getBalance(), 0.001);
        }

        @Test
        @DisplayName("Creating CREDIT account succeeds when user has no existing CREDIT")
        void createCredit_success() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "CREDIT")).thenReturn(false);

            List<Account> accounts = accountService.createAccountForUser(user, "CREDIT", null);

            assertEquals(1, accounts.size());
            assertEquals("CREDIT", accounts.get(0).getAccountType());
            assertEquals(0.0, accounts.get(0).getBalance(), 0.001);
        }

        @Test
        @DisplayName("Creating BOTH accounts succeeds")
        void createBoth_success() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "DEBIT")).thenReturn(false);
            when(accountRepository.userHasAccountType(1L, "CREDIT")).thenReturn(false);

            List<Account> accounts = accountService.createAccountForUser(user, "BOTH", 100.0);

            assertEquals(2, accounts.size());

        }

        @Test
        @DisplayName("Creating duplicate DEBIT account throws IllegalArgumentException")
        void createDebit_duplicate_throws() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "DEBIT")).thenReturn(true);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.createAccountForUser(user, "DEBIT", 0.0));
        }
    }

    // ─── toAccountResponse ──────────────────────────────────────────────────

    @Nested
    @DisplayName("toAccountResponse()")
    class ToAccountResponseTests {

        @Test
        @DisplayName("Maps Account entity fields correctly to AccountResponse DTO")
        void toAccountResponse_mapping() {
            User user = makeUser(5L);
            Account account = makeDebitAccount(10L, user, 999.0);
            account.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));

            DTORequest.AccountResponse resp = accountService.toAccountResponse(account);

            assertEquals(10L, resp.getId());
            assertEquals(5L, resp.getUserId());
            assertEquals("DEBIT123456789", resp.getAccountNumber());
            assertEquals(999.0, resp.getBalance(), 0.001);
            assertEquals("DEBIT", resp.getAccountType());
            assertEquals(account.getCreditLimit(), resp.getCreditLimit());
            assertNotNull(resp.getCreationAt());
        }
    }
}