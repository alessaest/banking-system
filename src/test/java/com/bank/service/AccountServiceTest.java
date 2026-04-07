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

            List<Account> accounts = accountService.createAccountForUser(user, "DEBIT", 500.0, 0.0);

            assertEquals(1, accounts.size());
            assertEquals("DEBIT", accounts.get(0).getAccountType());
            assertEquals(500.0, accounts.get(0).getBalance(), 0.001);
        }

        @Test
        @DisplayName("Creating CREDIT account succeeds when user has no existing CREDIT")
        void createCredit_success() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "CREDIT")).thenReturn(false);

            List<Account> accounts = accountService.createAccountForUser(user, "CREDIT", null, 0.0);

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

            List<Account> accounts = accountService.createAccountForUser(user, "BOTH", 100.0, 0.0);

            assertEquals(2, accounts.size());

        }

        @Test
        @DisplayName("Creating duplicate DEBIT account throws IllegalArgumentException")
        void createDebit_duplicate_throws() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "DEBIT")).thenReturn(true);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.createAccountForUser(user, "DEBIT", 0.0, 0.0));
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

// ─── Savings Account Creation ───────────────────────────────────────────

    @Nested
    @DisplayName("createSavingsAccount()")
    class CreateSavingsAccountTests {

        @Test
        @DisplayName("Creating SAVINGS account with valid parameters succeeds")
        void createSavings_success() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "SAVINGS")).thenReturn(false);

            Account result = accountService.createSavingsAccount(user, 1000.0, 2.5);

            assertNotNull(result);
            assertEquals("SAVINGS", result.getAccountType());
            assertEquals(1000.0, result.getBalance(), 0.001);
            assertEquals(2.5, result.getInterestRate(), 0.001);
            verify(accountRepository).persist(any(Account.class));
        }

        @Test
        @DisplayName("Creating SAVINGS account with null balance defaults to 0.0")
        void createSavings_null_balance() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "SAVINGS")).thenReturn(false);

            Account result = accountService.createSavingsAccount(user, null, 2.5);

            assertNotNull(result);
            assertEquals(0.0, result.getBalance(), 0.001);
        }

        @Test
        @DisplayName("Creating duplicate SAVINGS account throws IllegalArgumentException")
        void createSavings_duplicate_throws() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "SAVINGS")).thenReturn(true);  // ← TRUE for duplicate check

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.createSavingsAccount(user, 500.0, 2.5));
        }

        @Test
        @DisplayName("Creating SAVINGS account with null interest rate throws IllegalArgumentException")
        void createSavings_null_rate_throws() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "SAVINGS")).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.createSavingsAccount(user, 500.0, null));
        }

        @Test
        @DisplayName("Creating SAVINGS account with negative interest rate throws IllegalArgumentException")
        void createSavings_negative_rate_throws() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "SAVINGS")).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.createSavingsAccount(user, 500.0, -2.5));
        }

        @Test
        @DisplayName("Creating SAVINGS account with interest rate exceeding 100 throws IllegalArgumentException")
        void createSavings_rate_exceeds_throws() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "SAVINGS")).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.createSavingsAccount(user, 500.0, 150.0));
        }

        @Test
        @DisplayName("Creating SAVINGS account with interest rate exactly 100 succeeds")
        void createSavings_rate_100_success() {
            User user = makeUser(1L);
            when(accountRepository.userHasAccountType(1L, "SAVINGS")).thenReturn(false);

            Account result = accountService.createSavingsAccount(user, 100.0, 100.0);

            assertNotNull(result);
            assertEquals(100.0, result.getInterestRate(), 0.001);
        }
    }


// ─── Deposit to Savings ─────────────────────────────────────────────────

    @Nested
    @DisplayName("depositToSavings()")
    class DepositToSavingsTests {

        @Test
        @DisplayName("Deposit positive amount into owned SAVINGS account succeeds")
        void depositToSavings_success() {
            User user = makeUser(1L);
            Account savingsAccount = new Account();
            savingsAccount.id = 30L;
            savingsAccount.setAccountNumber("SAV123456789");
            savingsAccount.setBalance(1000.0);
            savingsAccount.setAccountType("SAVINGS");
            savingsAccount.setUser(user);
            savingsAccount.setInterestRate(3.5);

            when(accountRepository.findByIdOptional(30L)).thenReturn(Optional.of(savingsAccount));

            DTORequest.TransactionResponse result = accountService.depositToSavings(30L, 250.0, 1L);

            assertNotNull(result);
            assertEquals(1250.0, savingsAccount.getBalance(), 0.001);
            assertEquals("DEPOSIT", result.getType());
            assertEquals("Completed", result.getStatus());
            verify(transactionRepository).persist(any(Transaction.class));
        }

        @Test
        @DisplayName("Deposit zero amount throws IllegalArgumentException")
        void depositToSavings_zero_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToSavings(30L, 0.0, 1L));
        }

        @Test
        @DisplayName("Deposit negative amount throws IllegalArgumentException")
        void depositToSavings_negative_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToSavings(30L, -50.0, 1L));
        }

        @Test
        @DisplayName("Deposit to non-SAVINGS account throws IllegalArgumentException")
        void depositToSavings_wrong_type_throws() {
            User user = makeUser(1L);
            Account debitAccount = makeDebitAccount(10L, user, 500.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(debitAccount));

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToSavings(10L, 100.0, 1L));
        }

        @Test
        @DisplayName("Deposit to SAVINGS account owned by another user throws IllegalArgumentException")
        void depositToSavings_unauthorized_throws() {
            User owner = makeUser(1L);
            Account savingsAccount = new Account();
            savingsAccount.id = 30L;
            savingsAccount.setAccountType("SAVINGS");
            savingsAccount.setUser(owner);

            when(accountRepository.findByIdOptional(30L)).thenReturn(Optional.of(savingsAccount));

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToSavings(30L, 100.0, 2L));
        }

        @Test
        @DisplayName("Deposit to non-existent SAVINGS account throws IllegalArgumentException")
        void depositToSavings_not_found_throws() {
            when(accountRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToSavings(99L, 100.0, 1L));
        }
    }

// ─── Update Credit Limit ────────────────────────────────────────────────

    @Nested
    @DisplayName("updateCreditLimit()")
    class UpdateCreditLimitTests {

        @Test
        @DisplayName("Admin updates credit account limit successfully")
        void updateCreditLimit_success() {
            User user = makeUser(1L);
            Account creditAccount = makeCreditAccount(20L, user, 500.0);
            creditAccount.setCreditLimit(1000.0);

            when(accountRepository.findByIdOptional(20L)).thenReturn(Optional.of(creditAccount));

            DTORequest.AccountResponse result = accountService.updateCreditLimit(20L, 2000.0);

            assertNotNull(result);
            assertEquals(3000.0, creditAccount.getCreditLimit(), 0.001);
            assertEquals(3000.0, creditAccount.getBalance(), 0.001);
        }

        @Test
        @DisplayName("Updating credit limit on DEBIT account throws IllegalArgumentException")
        void updateCreditLimit_on_debit_throws() {
            User user = makeUser(1L);
            Account debitAccount = makeDebitAccount(10L, user, 500.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(debitAccount));

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateCreditLimit(10L, 1000.0));
        }

        @Test
        @DisplayName("Updating credit limit with zero throws IllegalArgumentException")
        void updateCreditLimit_zero_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateCreditLimit(20L, 0.0));
        }

        @Test
        @DisplayName("Updating credit limit with negative throws IllegalArgumentException")
        void updateCreditLimit_negative_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateCreditLimit(20L, -500.0));
        }

        @Test
        @DisplayName("Updating non-existent account throws IllegalArgumentException")
        void updateCreditLimit_not_found_throws() {
            when(accountRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateCreditLimit(99L, 1000.0));
        }

        @Test
        @DisplayName("Updating credit limit with null creditLimit defaults to 0.0")
        void updateCreditLimit_null_existing_limit() {
            User user = makeUser(1L);
            Account creditAccount = makeCreditAccount(20L, user, 0.0);
            creditAccount.setCreditLimit(null);  // Explicitly null

            when(accountRepository.findByIdOptional(20L)).thenReturn(Optional.of(creditAccount));

            DTORequest.AccountResponse result = accountService.updateCreditLimit(20L, 500.0);

            assertNotNull(result);
            assertEquals(500.0, creditAccount.getCreditLimit(), 0.001);
        }
    }

// ─── Update Savings Interest Rate ───────────────────────────────────────

    @Nested
    @DisplayName("updateSavingsInterestRate()")
    class UpdateSavingsInterestRateTests {

        @Test
        @DisplayName("Admin updates savings account interest rate successfully")
        void updateSavingsInterestRate_success() {
            User user = makeUser(1L);
            Account savingsAccount = new Account();
            savingsAccount.id = 30L;
            savingsAccount.setAccountType("SAVINGS");
            savingsAccount.setUser(user);
            savingsAccount.setInterestRate(2.5);

            when(accountRepository.findByIdOptional(30L)).thenReturn(Optional.of(savingsAccount));

            DTORequest.AccountResponse result = accountService.updateSavingsInterestRate(30L, 5.0);

            assertNotNull(result);
            assertEquals(5.0, savingsAccount.getInterestRate(), 0.001);
            verify(accountRepository).persist(savingsAccount);
        }

        @Test
        @DisplayName("Updating interest rate on non-SAVINGS account throws IllegalArgumentException")
        void updateSavingsInterestRate_on_debit_throws() {
            User user = makeUser(1L);
            Account debitAccount = makeDebitAccount(10L, user, 500.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(debitAccount));

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateSavingsInterestRate(10L, 3.5));
        }

        @Test
        @DisplayName("Updating interest rate with null throws IllegalArgumentException")
        void updateSavingsInterestRate_null_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateSavingsInterestRate(30L, null));
        }

        @Test
        @DisplayName("Updating interest rate with negative throws IllegalArgumentException")
        void updateSavingsInterestRate_negative_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateSavingsInterestRate(30L, -1.5));
        }

        @Test
        @DisplayName("Updating interest rate > 100 throws IllegalArgumentException")
        void updateSavingsInterestRate_exceeds_100_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateSavingsInterestRate(30L, 150.0));
        }

        @Test
        @DisplayName("Updating interest rate to exactly 100 succeeds")
        void updateSavingsInterestRate_100_success() {
            User user = makeUser(1L);
            Account savingsAccount = new Account();
            savingsAccount.id = 30L;
            savingsAccount.setAccountType("SAVINGS");
            savingsAccount.setUser(user);

            when(accountRepository.findByIdOptional(30L)).thenReturn(Optional.of(savingsAccount));

            DTORequest.AccountResponse result = accountService.updateSavingsInterestRate(30L, 100.0);

            assertNotNull(result);
            assertEquals(100.0, savingsAccount.getInterestRate(), 0.001);
        }

        @Test
        @DisplayName("Updating interest rate for non-existent account throws IllegalArgumentException")
        void updateSavingsInterestRate_not_found_throws() {
            when(accountRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateSavingsInterestRate(99L, 3.5));
        }
    }

// ─── Deposit to Credit edge cases ───────────────────────────────────────

    @Nested
    @DisplayName("depositToCredit() — edge cases")
    class DepositToCreditEdgeCasesTests {

        @Test
        @DisplayName("Deposit when creditLimit is null throws IllegalArgumentException")
        void depositToCredit_null_limit_throws() {
            User user = makeUser(1L);
            Account creditAccount = makeCreditAccount(20L, user, 0.0);
            creditAccount.setCreditLimit(null);

            when(accountRepository.findByIdOptional(20L)).thenReturn(Optional.of(creditAccount));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToCredit(20L, 100.0, 1L));
            assertTrue(ex.getMessage().contains("no limit set"));
        }

        @Test
        @DisplayName("Deposit when creditLimit is 0 throws IllegalArgumentException")
        void depositToCredit_zero_limit_throws() {
            User user = makeUser(1L);
            Account creditAccount = makeCreditAccount(20L, user, 0.0);
            creditAccount.setCreditLimit(0.0);

            when(accountRepository.findByIdOptional(20L)).thenReturn(Optional.of(creditAccount));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToCredit(20L, 100.0, 1L));
            assertTrue(ex.getMessage().contains("no limit set"));
        }

        @Test
        @DisplayName("Deposit exactly at credit limit boundary succeeds")
        void depositToCredit_at_boundary_success() {
            User user = makeUser(1L);
            Account creditAccount = makeCreditAccount(20L, user, 500.0);
            creditAccount.setCreditLimit(1000.0);

            when(accountRepository.findByIdOptional(20L)).thenReturn(Optional.of(creditAccount));

            DTORequest.TransactionResponse result = accountService.depositToCredit(20L, 500.0, 1L);

            assertNotNull(result);
            assertEquals(1000.0, creditAccount.getBalance(), 0.001);
        }
    }

}
