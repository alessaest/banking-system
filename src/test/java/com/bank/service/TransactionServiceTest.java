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
class TransactionServiceTest extends BaseServiceTest {

    @Inject
    TransactionService transactionService;

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountService accountService;

    @Inject
    UserRepository userRepository;

    // ─── Helper Methods ────────────────────────────────────────────────

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
        return accounts.getFirst(); // FIXED: Changed from .get(0) to .getFirst()
    }

    @Transactional
    protected Account createCreditAccount(User owner) {
        List<Account> accounts = accountService.createAccountForUser(owner, "CREDIT", null, 0.0);
        Account creditAccount = accounts.getFirst(); // FIXED: Changed from .get(0) to .getFirst()
        creditAccount.setCreditLimit(1000.0);
        accountRepository.persist(creditAccount);
        return creditAccount;
    }

    // ─── Transfer Tests ────────────────────────────────────────────────

    @Nested
    @DisplayName("transferMoney()")
    class TransferTests {

        @Test
        @DisplayName("Valid transfer between two different accounts succeeds")
        //@Transactional
        void transfer_success() {
            // Arrange
            User userA = createUser("userA_transfer", "userA@example.com");
            User userB = createUser("userB_transfer", "userB@example.com");
            Account from = createDebitAccount(userA, 1000.0);
            Account to = createDebitAccount(userB, 200.0);

            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(from.id);
            req.setToAccountId(to.id);
            req.setAmount(300.0);
            req.setDescription("Rent payment");

            // Act
            DTORequest.TransactionResponse result = transactionService.transferMoney(req, userA.id);

            // Assert
            assertNotNull(result);
            assertEquals("TRANSFER", result.getType());

            // Verify balances in database
            Account fromUpdated = accountRepository.findByIdOptional(from.id).orElseThrow();
            Account toUpdated = accountRepository.findByIdOptional(to.id).orElseThrow();
            assertEquals(700.0, fromUpdated.getBalance(), 0.001);
            assertEquals(500.0, toUpdated.getBalance(), 0.001);

            // Verify transaction was created
            List<Transaction> transactions = transactionRepository.listAll();
            assertTrue(transactions.stream().anyMatch(tx -> tx.getType().equals("TRANSFER")));
        }

        @Test
        @DisplayName("Transfer to same account throws IllegalArgumentException")
        void transfer_same_account_throws() {
            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(10L);
            req.setToAccountId(10L);
            req.setAmount(100.0);

            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.transferMoney(req, 1L));
        }

        @Test
        @DisplayName("Transfer with zero amount throws IllegalArgumentException")
        void transfer_zero_amount_throws() {
            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(10L);
            req.setToAccountId(20L);
            req.setAmount(0.0);

            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.transferMoney(req, 1L));
        }

        @Test
        @DisplayName("Transfer with negative amount throws IllegalArgumentException")
        void transfer_negative_amount_throws() {
            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(10L);
            req.setToAccountId(20L);
            req.setAmount(-50.0);

            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.transferMoney(req, 1L));
        }

        @Test
        @DisplayName("Transfer from another user's account throws IllegalArgumentException")
        //@Transactional
        void transfer_unauthorized_throws() {
            User owner = createUser("owner_transfer", "owner@example.com");
            User hacker = createUser("hacker_transfer", "hacker@example.com");
            Account from = createDebitAccount(owner, 500.0);
            Account to = createDebitAccount(hacker, 0.0);

            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(from.id);
            req.setToAccountId(to.id);
            req.setAmount(100.0);

            // Hacker tries to transfer from owner's account
            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.transferMoney(req, hacker.id));
        }

        @Test
        @DisplayName("Transfer with insufficient balance throws IllegalArgumentException")
        //@Transactional
        void transfer_insufficient_balance_throws() {
            User userA = createUser("userA_insuff", "userA_insuff@example.com");
            User userB = createUser("userB_insuff", "userB_insuff@example.com");
            Account from = createDebitAccount(userA, 50.0);
            Account to = createDebitAccount(userB, 0.0);

            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(from.id);
            req.setToAccountId(to.id);
            req.setAmount(200.0);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.transferMoney(req, userA.id));
            assertTrue(ex.getMessage().toLowerCase().contains("insufficient"));
        }

        @Test
        @DisplayName("Transfer from non-existent account throws IllegalArgumentException")
        void transfer_from_account_not_found_throws() {
            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(99999L);
            req.setToAccountId(20L);
            req.setAmount(100.0);

            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.transferMoney(req, 1L));
        }

        @Test
        @DisplayName("Transfer to non-existent account throws IllegalArgumentException")
        //@Transactional
        void transfer_to_account_not_found_throws() {
            User user = createUser("user_txn_notfound", "user_txn_notfound@example.com");
            Account from = createDebitAccount(user, 500.0);

            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(from.id);
            req.setToAccountId(99999L);
            req.setAmount(100.0);

            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.transferMoney(req, user.id));
        }
    }

    // ─── Deposit via TransactionService ────────────────────────────────

    @Nested
    @DisplayName("deposit() — delegates to AccountService (DEPRECATED)")
    class DepositDelegationTests {

        @Test
        @DisplayName("Delegates DepositRequest to AccountService.depositToDebit for DEBIT account")
        //@Transactional
        void deposit_delegates_correctly() {
            User user = createUser("dep_debit", "dep_debit@example.com");
            Account account = createDebitAccount(user, 1000.0);

            DTORequest.DepositRequest req = new DTORequest.DepositRequest();
            req.setAccountId(account.id);
            req.setAmount(250.0);

            @SuppressWarnings("deprecation")
            DTORequest.TransactionResponse result = transactionService.deposit(req, user.id);

            assertNotNull(result);
            assertEquals("DEPOSIT", result.getType());

            Account updated = accountRepository.findByIdOptional(account.id).orElseThrow();
            assertEquals(1250.0, updated.getBalance(), 0.001);
        }

        @Test
        @DisplayName("Delegates DepositRequest to AccountService.depositToCredit for CREDIT account")
        //@Transactional
        void deposit_delegates_to_credit_correctly() {
            User user = createUser("dep_credit", "dep_credit@example.com");
            Account account = createCreditAccount(user);

            DTORequest.DepositRequest req = new DTORequest.DepositRequest();
            req.setAccountId(account.id);
            req.setAmount(250.0);

            @SuppressWarnings("deprecation")
            DTORequest.TransactionResponse result = transactionService.deposit(req, user.id);

            assertNotNull(result);
            assertEquals("DEPOSIT", result.getType());

            Account updated = accountRepository.findByIdOptional(account.id).orElseThrow();
            assertEquals(250.0, updated.getBalance(), 0.001);
        }

        @Test
        @DisplayName("Deposit to SAVINGS account throws IllegalArgumentException")
        //@Transactional
        void deposit_savings_throws_invalid_type() {
            User user = createUser("dep_savings", "dep_savings@example.com");
            List<Account> accounts = accountService.createAccountForUser(user, "SAVINGS", null, 1000.0);
            Account savingsAccount = accounts.getFirst(); // FIXED: Changed from .get(0) to .getFirst()

            DTORequest.DepositRequest req = new DTORequest.DepositRequest();
            req.setAccountId(savingsAccount.id);
            req.setAmount(500.0);

            @SuppressWarnings("deprecation")
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.deposit(req, user.id));
            assertTrue(ex.getMessage().toLowerCase().contains("invalid account type"));
        }
    }

    // ─── Transaction History Tests ─────────────────────────────────────

    @Nested
    @DisplayName("getAccountTransactionHistory() — user-scoped filtering")
    class HistoryTests {

        @Test
        @DisplayName("Returns only transactions for the requesting user's account")
        //@Transactional
        void history_returns_only_own_account_transactions() {
            User user = createUser("hist_user", "hist_user@example.com");
            Account account = createDebitAccount(user, 500.0);

            // Perform deposits to create transactions
            DTORequest.DepositRequest req1 = new DTORequest.DepositRequest();
            req1.setAccountId(account.id);
            req1.setAmount(100.0);

            DTORequest.DepositRequest req2 = new DTORequest.DepositRequest();
            req2.setAccountId(account.id);
            req2.setAmount(200.0);

            transactionService.deposit(req1, user.id);
            transactionService.deposit(req2, user.id);

            List<DTORequest.TransactionResponse> history =
                    transactionService.getAccountTransactionHistory(account.id, user.id);

            assertEquals(2, history.size());
            assertTrue(history.stream().allMatch(t -> account.id.equals(t.getToAccountId())));
        }

        @Test
        @DisplayName("Requesting history of another user's account throws IllegalArgumentException")
        //@Transactional
        void history_unauthorized_throws() {
            User user1 = createUser("hist_user1", "hist_user1@example.com");
            User user2 = createUser("hist_user2", "hist_user2@example.com");
            Account account = createDebitAccount(user1, 500.0);

            // User 2 tries to see user 1's history
            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.getAccountTransactionHistory(account.id, user2.id));
        }

        @Test
        @DisplayName("Requesting history for non-existent account throws IllegalArgumentException")
        void history_account_not_found_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.getAccountTransactionHistory(99999L, 1L));
        }

        @Test
        @DisplayName("Empty history returns empty list")
        //@Transactional
        void history_empty_list() {
            User user = createUser("hist_empty", "hist_empty@example.com");
            Account account = createDebitAccount(user, 500.0);

            List<DTORequest.TransactionResponse> history =
                    transactionService.getAccountTransactionHistory(account.id, user.id);

            assertNotNull(history);
            assertTrue(history.isEmpty());
        }
    }

    // ─── Get Transaction By ID ─────────────────────────────────────────

    @Nested
    @DisplayName("getTransactionById()")
    class GetByIdTests {

        @Test
        @DisplayName("Returns TransactionResponse when transaction exists")
        //@Transactional
        void getById_found() {
            User user = createUser("tx_user", "tx_user@example.com");
            Account acc = createDebitAccount(user, 500.0);

            // Create a transaction
            DTORequest.DepositRequest req = new DTORequest.DepositRequest();
            req.setAccountId(acc.id);
            req.setAmount(100.0);
            transactionService.deposit(req, user.id);

            List<Transaction> allTx = transactionRepository.listAll();
            assertFalse(allTx.isEmpty());

            Transaction tx = allTx.getFirst(); // FIXED: Changed from .get(0) to .getFirst()
            Optional<DTORequest.TransactionResponse> result = transactionService.getTransactionById(tx.id);

            assertTrue(result.isPresent());
            assertEquals(tx.id, result.get().getId());
        }

        @Test
        @DisplayName("Returns empty Optional when transaction does not exist")
        void getById_not_found() {
            Optional<DTORequest.TransactionResponse> result = transactionService.getTransactionById(99999L);
            assertFalse(result.isPresent());
        }
    }

    // ─── Transaction History by Type ───────────────────────────────────

    @Nested
    @DisplayName("getAccountTransactionHistoryByType()")
    class HistoryByTypeTests {

        @Test
        @DisplayName("Returns only DEPOSIT transactions when type filter is 'DEPOSIT'")
        //@Transactional
        void historyByType_deposit_filter() {
            User user = createUser("hist_type_dep", "hist_type_dep@example.com");
            Account account = createDebitAccount(user, 500.0);

            // Create deposits
            DTORequest.DepositRequest req1 = new DTORequest.DepositRequest();
            req1.setAccountId(account.id);
            req1.setAmount(100.0);

            DTORequest.DepositRequest req2 = new DTORequest.DepositRequest();
            req2.setAccountId(account.id);
            req2.setAmount(200.0);

            transactionService.deposit(req1, user.id);
            transactionService.deposit(req2, user.id);

            List<DTORequest.TransactionResponse> history =
                    transactionService.getAccountTransactionHistoryByType(account.id, user.id, "DEPOSIT");

            assertEquals(2, history.size());
            assertTrue(history.stream().allMatch(t -> "DEPOSIT".equals(t.getType())));
        }

        @Test
        @DisplayName("Returns only WITHDRAWAL transactions when type filter is 'WITHDRAWAL'")
        //@Transactional
        void historyByType_withdrawal_filter() {
            User user = createUser("hist_type_with", "hist_type_with@example.com");
            Account account = createDebitAccount(user, 500.0);

            // Create a withdrawal
            accountService.withdraw(account.id, 50.0, user.id);

            List<DTORequest.TransactionResponse> history =
                    transactionService.getAccountTransactionHistoryByType(account.id, user.id, "WITHDRAWAL");

            assertEquals(1, history.size());
            assertEquals("WITHDRAWAL", history.getFirst().getType()); // FIXED: Changed from .get(0) to .getFirst()
        }

        @Test
        @DisplayName("Returns only TRANSFER transactions when type filter is 'TRANSFER'")
        //@Transactional
        void historyByType_transfer_filter() {
            User user1 = createUser("hist_type_trans1", "hist_type_trans1@example.com");
            User user2 = createUser("hist_type_trans2", "hist_type_trans2@example.com");
            Account account1 = createDebitAccount(user1, 500.0);
            Account account2 = createDebitAccount(user2, 100.0);

            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(account1.id);
            req.setToAccountId(account2.id);
            req.setAmount(75.0);

            transactionService.transferMoney(req, user1.id);

            List<DTORequest.TransactionResponse> history =
                    transactionService.getAccountTransactionHistoryByType(account1.id, user1.id, "TRANSFER");

            assertEquals(1, history.size());
            assertEquals("TRANSFER", history.getFirst().getType()); // FIXED: Changed from .get(0) to .getFirst()
        }

        @Test
        @DisplayName("Invalid type filter throws IllegalArgumentException")
        //@Transactional
        void historyByType_invalid_type_throws() {
            User user = createUser("hist_invalid", "hist_invalid@example.com");
            Account account = createDebitAccount(user, 500.0);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.getAccountTransactionHistoryByType(account.id, user.id, "INVALID"));
            assertTrue(ex.getMessage().toLowerCase().contains("invalid type"));
        }

        @Test
        @DisplayName("Non-owner requesting type-filtered history throws IllegalArgumentException")
        //@Transactional
        void historyByType_unauthorized_throws() {
            User user1 = createUser("hist_unauth1", "hist_unauth1@example.com");
            User user2 = createUser("hist_unauth2", "hist_unauth2@example.com");
            Account account = createDebitAccount(user1, 500.0);

            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.getAccountTransactionHistoryByType(account.id, user2.id, "DEPOSIT"));
        }

        @Test
        @DisplayName("Empty type-filtered history returns empty list")
        //@Transactional
        void historyByType_empty_list() {
            User user = createUser("hist_empty_type", "hist_empty_type@example.com");
            Account account = createDebitAccount(user, 500.0);

            List<DTORequest.TransactionResponse> history =
                    transactionService.getAccountTransactionHistoryByType(account.id, user.id, "TRANSFER");

            assertNotNull(history);
            assertTrue(history.isEmpty());
        }
    }

    // ─── Transfer null field guards ────────────────────────────────────

    @Nested
    @DisplayName("transferMoney() — null field guards")
    class TransferNullGuardsTests {

        @Test
        //@DisplayName("Transfer with null amount throws IllegalArgumentException")
        void transfer_null_amount_throws() {
            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(10L);
            req.setToAccountId(20L);
            req.setAmount(null);

            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.transferMoney(req, 1L));
        }
    }
}
