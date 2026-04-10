package com.bank.service;

import com.bank.dto.DTORequest;
import com.bank.entity.Account;
import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AccountServiceTest extends BaseServiceTest {

    @Inject
    AccountService accountService;

    @Inject
    AccountRepository accountRepository;

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    UserRepository userRepository;

    // ─── Helper Methods to Create Real DB Objects ───────────────────────

    @Transactional
    protected User createUser(String username, String email) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("hashedPassword123");
        u.setFirstName("Test");
        u.setLastName("User");
        u.setRole("user");
        u.setCreatedAt(LocalDateTime.now());
        userRepository.persist(u);
        return u;
    }

    @Transactional
    protected Account createDebitAccount(User owner, double balance) {
        List<Account> accounts = accountService.createAccountForUser(owner, "DEBIT", balance, 0.0);
        return accounts.getFirst();
    }

    @Transactional
    protected Account createCreditAccount(User owner) {
        List<Account> accounts = accountService.createAccountForUser(owner, "CREDIT", null, 0.0);
        return accounts.getFirst();
    }

    @Transactional
    protected Account createSavingsAccount(User owner, Double balance, Double rate) {
        List<Account> accounts = accountService.createAccountForUser(owner, "SAVINGS", null, balance);
        Account savings = accounts.getFirst();
        savings.setInterestRate(rate);
        accountRepository.persist(savings);
        return savings;
    }

    @Transactional
    protected Account createCreditAccountWithLimit(User owner, Double limit) {
        List<Account> accounts = accountService.createAccountForUser(owner, "CREDIT", null, 0.0);
        Account credit = accounts.getFirst();
        credit.setCreditLimit(limit);
        accountRepository.persist(credit);
        return credit;
    }

    // ─── Deposit Tests with Real Database ───────────────────────────────

    @Nested
    @DisplayName("deposit()")
    class DepositTests {

        @Test
        void deposit_debit_success() {
            User user = createUser("testuser", "test@example.com");
            Account account = createDebitAccount(user, 500.0);

            DTORequest.TransactionResponse result = accountService.depositToDebit(account.id, 200.0, user.id, false);

            assertNotNull(result);
            assertEquals("DEPOSIT", result.getType());
            assertEquals("Completed", result.getStatus());

            Account updated = accountRepository.findByIdOptional(account.id).orElseThrow();
            assertEquals(700.0, updated.getBalance(), 0.001);

            List<Transaction> transactions = transactionRepository.listAll();
            assertFalse(transactions.isEmpty());
            assertTrue(transactions.stream().anyMatch(tx -> tx.getType().equals("DEPOSIT")));
        }

        @Test
        void deposit_zero_amount_throws() {
            User user = createUser("testuser2", "test2@example.com");
            Account account = createDebitAccount(user, 500.0);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToDebit(account.id, 0.0, user.id, false));
        }

        @Test
        void deposit_account_not_found_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToDebit(99999L, 100.0, 1L, false));
        }

        @Test
        void deposit_unauthorized_user_throws() {
            User user1 = createUser("user1", "user1@example.com");
            User user2 = createUser("user2", "user2@example.com");
            Account account = createDebitAccount(user1, 500.0);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToDebit(account.id, 100.0, user2.id, false));
        }

        @Test
        void deposit_negative_amount_throws() {
            User user = createUser("negdeposit", "negdeposit@example.com");
            Account account = createDebitAccount(user, 500.0);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToDebit(account.id, -100.0, user.id, false));
        }
    }

    // ─── Credit Account Deposit Tests ───────────────────────────────────

    @Nested
    @DisplayName("depositToCredit()")
    class CreditAccountDepositTests {

        @Test
        void deposit_credit_success() {
            User user = createUser("credituser", "credituser@example.com");
            Account account = createCreditAccountWithLimit(user, 1000.0);

            DTORequest.TransactionResponse result = accountService.depositToCredit(account.id, 500.0, user.id, false);

            assertNotNull(result);
            assertEquals("DEPOSIT", result.getType());

            Account updated = accountRepository.findByIdOptional(account.id).orElseThrow();
            assertEquals(500.0, updated.getBalance(), 0.001);
        }

        @Test
        void deposit_credit_no_limit_throws() {
            User user = createUser("creditnolimit", "creditnolimit@example.com");
            Account account = createCreditAccount(user);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToCredit(account.id, 100.0, user.id, false));
        }

        @Test
        void deposit_credit_exceeds_limit_throws() {
            User user = createUser("creditexceed", "creditexceed@example.com");
            Account account = createCreditAccountWithLimit(user, 500.0);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToCredit(account.id, 600.0, user.id, false));
        }
    }

    // ─── Savings Account Deposit Tests ──────────────────────────────────

    @Nested
    @DisplayName("depositToSavings()")
    class SavingsAccountDepositTests {

        @Test
        void deposit_savings_success() {
            User user = createUser("savingsuser", "savingsuser@example.com");
            Account account = createSavingsAccount(user, 1000.0, 2.5);

            DTORequest.TransactionResponse result = accountService.depositToSavings(account.id, 500.0, user.id, false);

            assertNotNull(result);
            assertEquals("DEPOSIT", result.getType());

            Account updated = accountRepository.findByIdOptional(account.id).orElseThrow();
            assertEquals(1500.0, updated.getBalance(), 0.001);
        }

        @Test
        void deposit_savings_zero_amount_throws() {
            User user = createUser("savingszero", "savingszero@example.com");
            Account account = createSavingsAccount(user, 1000.0, 2.5);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToSavings(account.id, 0.0, user.id, false));
        }

        @Test
        void deposit_savings_negative_amount_throws() {
            User user = createUser("savingsneg", "savingsneg@example.com");
            Account account = createSavingsAccount(user, 1000.0, 2.5);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToSavings(account.id, -100.0, user.id, false));
        }

        @Test
        void deposit_savings_unauthorized_throws() {
            User user1 = createUser("savingsuser1", "savingsuser1@example.com");
            User user2 = createUser("savingsuser2", "savingsuser2@example.com");
            Account account = createSavingsAccount(user1, 1000.0, 2.5);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.depositToSavings(account.id, 200.0, user2.id, false));
        }
    }

    // ─── Withdraw Tests ─────────────────────────────────────────────────

    @Nested
    @DisplayName("withdraw()")
    class WithdrawTests {

        @Test
        void withdraw_success() {
            User user = createUser("testuser3", "test3@example.com");
            Account account = createDebitAccount(user, 500.0);

            DTORequest.TransactionResponse result = accountService.withdraw(account.id, 200.0, user.id);

            assertNotNull(result);
            assertEquals("WITHDRAWAL", result.getType());

            Account updated = accountRepository.findByIdOptional(account.id).orElseThrow();
            assertEquals(300.0, updated.getBalance(), 0.001);
        }

        @Test
        void withdraw_insufficient_balance_throws() {
            User user = createUser("testuser4", "test4@example.com");
            Account account = createDebitAccount(user, 100.0);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> accountService.withdraw(account.id, 500.0, user.id));
            assertTrue(ex.getMessage().toLowerCase().contains("insufficient"));
        }

        @Test
        void withdraw_unauthorized_throws() {
            User user1 = createUser("user1a", "user1a@example.com");
            User user2 = createUser("user2a", "user2a@example.com");
            Account account = createDebitAccount(user1, 500.0);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.withdraw(account.id, 100.0, user2.id));
        }
    }

    // ─── Create Account Tests ───────────────────────────────────────────

    @Nested
    @DisplayName("createAccountForUser()")
    class CreateAccountTests {

        @Test
        void createDebit_success() {
            User user = createUser("createtest1", "createtest1@example.com");

            List<Account> accounts = accountService.createAccountForUser(user, "DEBIT", 500.0, 0.0);

            assertEquals(1, accounts.size());
            assertEquals("DEBIT", accounts.getFirst().getAccountType());
            assertEquals(500.0, accounts.getFirst().getBalance(), 0.001);

            List<Account> userAccounts = accountRepository.findByUserId(user.id);
            assertTrue(userAccounts.stream().anyMatch(a -> a.getAccountType().equals("DEBIT")));
        }

        @Test
        void createDebit_duplicate_throws() {
            User user = createUser("duptest", "duptest@example.com");

            accountService.createAccountForUser(user, "DEBIT", 500.0, 0.0);

            assertThrows(IllegalStateException.class,
                    () -> accountService.createAccountForUser(user, "DEBIT", 500.0, 0.0));
        }

        @Test
        void createCredit_success() {
            User user = createUser("createcredit", "createcredit@example.com");

            List<Account> accounts = accountService.createAccountForUser(user, "CREDIT", null, 0.0);

            assertEquals(1, accounts.size());
            assertEquals("CREDIT", accounts.getFirst().getAccountType());
        }

        @Test
        void createSavings_success() {
            User user = createUser("createsavings", "createsavings@example.com");

            List<Account> accounts = accountService.createAccountForUser(user, "SAVINGS", null, 1000.0);

            assertEquals(1, accounts.size());
            assertEquals("SAVINGS", accounts.getFirst().getAccountType());
            assertEquals(1000.0, accounts.getFirst().getBalance(), 0.001);
        }
    }

    // ─── Get Balance Tests ──────────────────────────────────────────────

    @Nested
    @DisplayName("getAccountBalance()")
    class GetBalanceTests {

        @Test
        void getBalance_success() {
            User user = createUser("baltest", "baltest@example.com");
            Account account = createDebitAccount(user, 750.0);

            Double balance = accountService.getAccountBalance(account.id, user.id);
            assertEquals(750.0, balance, 0.001);
        }

        @Test
        void getBalance_unauthorized_throws() {
            User user1 = createUser("baltest1", "baltest1@example.com");
            User user2 = createUser("baltest2", "baltest2@example.com");
            Account account = createDebitAccount(user1, 750.0);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.getAccountBalance(account.id, user2.id));
        }
    }

    // ─── Credit Limit Update Tests ──────────────────────────────────────

    @Nested
    @DisplayName("updateCreditLimit()")
    class UpdateCreditLimitTests {

        @Test
        void updateCreditLimit_success() {
            User user = createUser("creditlimituser", "creditlimituser@example.com");
            Account account = createCreditAccountWithLimit(user, 1000.0);

            DTORequest.AccountResponse result = accountService.updateCreditLimit(account.id, 500.0);

            assertNotNull(result);
            assertEquals(1500.0, result.getCreditLimit(), 0.001);

            Account updated = accountRepository.findByIdOptional(account.id).orElseThrow();
            assertEquals(1500.0, updated.getCreditLimit(), 0.001);
        }

        @Test
        void updateCreditLimit_debit_account_throws() {
            User user = createUser("debitlimituser", "debitlimituser@example.com");
            Account account = createDebitAccount(user, 500.0);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateCreditLimit(account.id, 500.0));
        }

        @Test
        void updateCreditLimit_negative_throws() {
            User user = createUser("neglimituser", "neglimituser@example.com");
            Account account = createCreditAccountWithLimit(user, 1000.0);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateCreditLimit(account.id, -500.0));
        }
    }

    // ─── Interest Rate Update Tests ─────────────────────────────────────

    @Nested
    @DisplayName("updateSavingsInterestRate()")
    class UpdateInterestRateTests {

        @Test
        void updateInterestRate_success() {
            User user = createUser("rateuser", "rateuser@example.com");
            Account account = createSavingsAccount(user, 1000.0, 2.5);

            DTORequest.AccountResponse result = accountService.updateSavingsInterestRate(account.id, 3.5);

            assertNotNull(result);
            assertEquals(3.5, result.interestRate, 0.001);

            Account updated = accountRepository.findByIdOptional(account.id).orElseThrow();
            assertEquals(3.5, updated.getInterestRate(), 0.001);
        }

        @Test
        void updateInterestRate_debit_account_throws() {
            User user = createUser("debitrateuser", "debitrateuser@example.com");
            Account account = createDebitAccount(user, 500.0);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateSavingsInterestRate(account.id, 5.0));
        }

        @Test
        void updateInterestRate_zero_throws() {
            User user = createUser("zerorateuser", "zerorateuser@example.com");
            Account account = createSavingsAccount(user, 1000.0, 2.5);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateSavingsInterestRate(account.id, 0.0));
        }

        @Test
        void updateInterestRate_negative_throws() {
            User user = createUser("negrateuser", "negrateuser@example.com");
            Account account = createSavingsAccount(user, 1000.0, 2.5);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateSavingsInterestRate(account.id, -5.0));
        }

        @Test
        void updateInterestRate_exceeds_100_throws() {
            User user = createUser("highrateuser", "highrateuser@example.com");
            Account account = createSavingsAccount(user, 1000.0, 2.5);

            assertThrows(IllegalArgumentException.class,
                    () -> accountService.updateSavingsInterestRate(account.id, 150.0));
        }
    }

    // ─── Account Deletion Tests ─────────────────────────────────────────

    @Nested
    @DisplayName("deleteAccount()")
    class DeleteAccountTests {

        @Test
        void deleteAccount_success() {
            User user = createUser("deluser", "deluser@example.com");
            Account account = createDebitAccount(user, 500.0);

            accountService.deleteAccount(account.id);

            Optional<Account> result = accountRepository.findByIdOptional(account.id);
            assertFalse(result.isPresent());
        }

        @Test
        void deleteAccount_not_found_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> accountService.deleteAccount(99999L));
        }

        @Test
        void deleteAccount_removes_associated_transactions() {
            User user = createUser("deltxuser", "deltxuser@example.com");
            Account account = createDebitAccount(user, 500.0);

            accountService.depositToDebit(account.id, 100.0, user.id, true);

            List<Transaction> txBefore = transactionRepository.listAll();
            assertTrue(txBefore.size() > 0);

            accountService.deleteAccount(account.id);

            List<Transaction> txAfter = transactionRepository.listAll();
            assertTrue(txAfter.isEmpty());
        }
    }
}
