package com.bank.service;

import com.bank.dto.DTORequest;
import com.bank.entity.Account;
import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@QuarkusTest
class TransactionServiceTest {

    @Inject
    TransactionService transactionService;

    @InjectMock
    TransactionRepository transactionRepository;

    @InjectMock
    AccountRepository accountRepository;

    @InjectMock
    AccountService accountService;

    // fixed test data

    private User makeUser(Long id) {
        User u = new User();
        u.id = id;
        u.setUsername("user" + id);
        u.setEmail("user" + id + "@example.com");
        u.setFirstName("User");
        u.setLastName(String.valueOf(id));
        u.setRole("user");
        return u;
    }

    private Account makeDebit(Long id, User owner, double balance) {
        Account a = new Account();
        a.id = id;
        a.setAccountNumber("ACC" + id);
        a.setAccountType("DEBIT");
        a.setBalance(balance);
        a.setUser(owner);
        return a;
    }

    private Account makeCredit(Long id, User owner, double balance) {
        Account a = new Account();
        a.id = id;
        a.setAccountNumber("CRD" + id);
        a.setAccountType("CREDIT");
        a.setBalance(balance);
        a.setUser(owner);
        return a;
    }

    private Transaction makeTx(Long id, Account from, Account to, double amount, String type) {
        Transaction tx = new Transaction();
        tx.id = id;
        tx.setFromAccount(from);
        tx.setToAccount(to);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setStatus("Completed");
        tx.setDescription("test");

        if (from != null) {
            tx.setUserId(from.getUser().id);
        } else if (to != null) {
            tx.setUserId(to.getUser().id);
        }
        return tx;
    }

    // ─── Transfer ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("transferMoney()")
    class TransferTests {

        @Test
        @DisplayName("Valid transfer between two different accounts succeeds")
        void transfer_success() {
            User userA = makeUser(1L);
            User userB = makeUser(2L);
            Account from = makeDebit(10L, userA, 1000.0);
            Account to   = makeDebit(20L, userB, 200.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(from));
            when(accountRepository.findByIdOptional(20L)).thenReturn(Optional.of(to));

            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(10L);
            req.setToAccountId(20L);
            req.setAmount(300.0);
            req.setDescription("Rent payment");

            Transaction stubTx = makeTx(1L, from, to, 300.0, "TRANSFER");
            DTORequest.TransactionResponse stubResp = new DTORequest.TransactionResponse(
                    1L, 10L, 20L, 1L, 300.0, "TRANSFER", "Completed", "Rent payment", null
            );
            when(accountService.toTransactionResponse(any())).thenReturn(stubResp);

            DTORequest.TransactionResponse result = transactionService.transferMoney(req, 1L);

            assertNotNull(result);
            assertEquals(700.0, from.getBalance(), 0.001);
            assertEquals(500.0, to.getBalance(), 0.001);
            assertEquals("TRANSFER", result.getType());
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
        void transfer_unauthorized_throws() {
            User owner = makeUser(1L);
            Account from = makeDebit(10L, owner, 500.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(from));

            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(10L);
            req.setToAccountId(20L);
            req.setAmount(100.0);

            // User 2 tries to transfer from user 1's account
            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.transferMoney(req, 2L));
        }

        @Test
        @DisplayName("Transfer with insufficient balance throws IllegalArgumentException")
        void transfer_insufficient_balance_throws() {
            User userA = makeUser(1L);
            User userB = makeUser(2L);
            Account from = makeDebit(10L, userA, 50.0);
            Account to   = makeDebit(20L, userB, 0.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(from));
            when(accountRepository.findByIdOptional(20L)).thenReturn(Optional.of(to));

            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(10L);
            req.setToAccountId(20L);
            req.setAmount(200.0);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> transactionService.transferMoney(req, 1L));
            assertTrue(ex.getMessage().toLowerCase().contains("insufficient"));
        }

        @Test
        @DisplayName("Transfer from non-existent account throws IllegalArgumentException")
        void transfer_from_account_not_found_throws() {
            when(accountRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

            DTORequest.TransferRequest req = new DTORequest.TransferRequest();
            req.setFromAccountId(99L);
            req.setToAccountId(20L);
            req.setAmount(100.0);

            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.transferMoney(req, 1L));
        }
    }

    // ─── Deposit via TransactionService ─────────────────────────────────────

    @Nested
    @DisplayName("deposit() — delegates to AccountService")
    class DepositDelegationTests {

        @Test
        @DisplayName("Delegates DepositRequest to AccountService.depositToDebit for DEBIT account")
        void deposit_delegates_correctly() {
            // Setup: Create a DEBIT account and mock it in repository
            User user = makeUser(1L);
            Account account = makeDebit(10L, user, 1000.0);  // DEBIT account

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(account));

            DTORequest.DepositRequest req = new DTORequest.DepositRequest();
            req.setAccountId(10L);
            req.setAmount(250.0);

            DTORequest.TransactionResponse stubResp = new DTORequest.TransactionResponse(
                    1L, null, 1L, 10L, 1250.0, "DEPOSIT", "Completed", "Deposit", null
            );
            when(accountService.depositToDebit(10L, 250.0, 1L)).thenReturn(stubResp);

            DTORequest.TransactionResponse result = transactionService.deposit(req, 1L);

            assertEquals(stubResp, result);
            verify(accountService).depositToDebit(10L, 250.0, 1L);
        }

        @Test
        @DisplayName("Delegates DepositRequest to AccountService.depositToCredit for CREDIT account")
        void deposit_delegates_to_credit_correctly() {
            // Setup: Create a CREDIT account and mock it in repository
            User user = makeUser(1L);
            Account account = makeCredit(20L, user, 0.0);  // CREDIT account
            account.setCreditLimit(1000.0);

            when(accountRepository.findByIdOptional(20L)).thenReturn(Optional.of(account));

            DTORequest.DepositRequest req = new DTORequest.DepositRequest();
            req.setAccountId(20L);
            req.setAmount(250.0);

            DTORequest.TransactionResponse stubResp = new DTORequest.TransactionResponse(
                    1L, null, 1L, 20L, 250.0, "DEPOSIT", "Completed", "Deposit", null
            );
            when(accountService.depositToCredit(20L, 250.0, 1L)).thenReturn(stubResp);

            DTORequest.TransactionResponse result = transactionService.deposit(req, 1L);

            assertEquals(stubResp, result);
            verify(accountService).depositToCredit(20L, 250.0, 1L);
        }
    }


    // ─── Transaction History (User-scoped) ──────────────────────────────────

    @Nested
    @DisplayName("getAccountTransactionHistory() — user-scoped filtering")
    class HistoryTests {

        @Test
        @DisplayName("Returns only transactions for the requesting user's account")
        void history_returns_only_own_account_transactions() {
            User user = makeUser(1L);
            Account account = makeDebit(10L, user, 500.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(account));

            Transaction tx1 = makeTx(1L, account, null, 100.0, "WITHDRAWAL");
            Transaction tx2 = makeTx(2L, null, account, 200.0, "DEPOSIT");
            when(transactionRepository.getAccountTransactionsForUser(10L, 1L)).thenReturn(List.of(tx1, tx2));

            DTORequest.TransactionResponse r1 = new DTORequest.TransactionResponse(
                    1L, 10L, null, 1L, 100.0, "WITHDRAWAL", "Completed", "", null);
            DTORequest.TransactionResponse r2 = new DTORequest.TransactionResponse(
                    2L, null, 10L, 1L, 200.0, "DEPOSIT", "Completed", "", null);

            when(accountService.toTransactionResponse(any(Transaction.class)))  // ← Use any()
                    .thenAnswer(invocation -> {
                        Transaction tx = invocation.getArgument(0);
                        return tx.id == 1L ? r1 : (tx.id == 2L ? r2 : null);
                    });

            List<DTORequest.TransactionResponse> history =
                    transactionService.getAccountTransactionHistory(10L, 1L);

            assertEquals(2, history.size());
        }

        @Test
        @DisplayName("Requesting history of another user's account throws IllegalArgumentException")
        void history_unauthorized_throws() {
            User owner = makeUser(1L);
            Account account = makeDebit(10L, owner, 500.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(account));

            // User 2 tries to see user 1's history
            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.getAccountTransactionHistory(10L, 2L));
        }

        @Test
        @DisplayName("Requesting history for non-existent account throws IllegalArgumentException")
        void history_account_not_found_throws() {
            when(accountRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> transactionService.getAccountTransactionHistory(99L, 1L));
        }

        @Test
        @DisplayName("Empty history returns empty list")
        void history_empty_list() {
            User user = makeUser(1L);
            Account account = makeDebit(10L, user, 500.0);

            when(accountRepository.findByIdOptional(10L)).thenReturn(Optional.of(account));
            when(transactionRepository.getAccountTransactionsForUser(10L, 1L)).thenReturn(List.of());

            List<DTORequest.TransactionResponse> history =
                    transactionService.getAccountTransactionHistory(10L, 1L);

            assertNotNull(history);
            assertTrue(history.isEmpty());
        }
    }

    // ─── Get Transaction By ID ───────────────────────────────────────────────

    @Nested
    @DisplayName("getTransactionById()")
    class GetByIdTests {

        @Test
        @DisplayName("Returns TransactionResponse when transaction exists")
        void getById_found() {
            User user = makeUser(1L);
            Account acc = makeDebit(10L, user, 500.0);
            Transaction tx = makeTx(1L, acc, null, 100.0, "WITHDRAWAL");

            when(transactionRepository.findTransactionsById(1L)).thenReturn(Optional.of(tx));
            DTORequest.TransactionResponse resp = new DTORequest.TransactionResponse(
                    1L, 10L, null, 1L, 100.0, "WITHDRAWAL", "Completed", "", null);
            when(accountService.toTransactionResponse(tx)).thenReturn(resp);

            Optional<DTORequest.TransactionResponse> result = transactionService.getTransactionById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
        }

        @Test
        @DisplayName("Returns empty Optional when transaction does not exist")
        void getById_not_found() {
            when(transactionRepository.findTransactionsById(99L)).thenReturn(Optional.empty());

            Optional<DTORequest.TransactionResponse> result = transactionService.getTransactionById(99L);

            assertFalse(result.isPresent());
        }
    }
}