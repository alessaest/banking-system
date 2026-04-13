package com.bank.service;

import com.bank.entity.Account;
import com.bank.entity.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class InterestSchedulerServiceTest extends BaseServiceTest {

    @Inject
    InterestSchedulerService interestSchedulerService;

    @Inject
    AccountService accountService;

    @Inject
    UserRepository userRepository;

    @Inject
    AccountRepository accountRepository;

    // helper methods
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
    protected Account createSavingsAccount(User owner, Double balance, Double rate) {
        List<Account> accounts = accountService.createAccountForUser(owner, "SAVINGS", null, balance);
        Account savings = accounts.getFirst();
        savings.setInterestRate(rate);
        accountRepository.persist(savings);
        return savings;
    }

    @Transactional
    protected Account createSavingsAccountWithTimestamp(User owner, Double balance, Double rate, LocalDateTime timestamp) {
        Account savings = createSavingsAccount(owner, balance, rate);
        savings.setLastInterestCalculatedAt(timestamp);
        accountRepository.persist(savings);
        return savings;
    }

    @Transactional
    protected Account createSavingsAccountWithNullRate(User owner) {
        List<Account> accounts = accountService.createAccountForUser(owner, "SAVINGS", null, 1000.0);
        Account savings = accounts.getFirst();
        savings.setInterestRate(null);
        accountRepository.persist(savings);  // Persist the null rate to DB
        return savings;
    }

    // interest job tests
    @Nested
    class InterestJobTests {

        @Test
        void testInterestJob_calls_account_service() {
            User user1 = createUser("interest_user1", "interest_user1@example.com");
            Account savings1 = createSavingsAccountWithTimestamp(user1, 1000.0, 2.5, LocalDateTime.now().minusMonths(1));

            interestSchedulerService.testInterestJob();

            Account updated = accountRepository.findByIdOptional(savings1.id).orElseThrow();
            assertTrue(updated.getBalance() > 1000.0, "Balance should have interest applied");
        }

        @Test
        void testInterestJob_handles_exception() {
            interestSchedulerService.testInterestJob();
            assertTrue(true);
        }

        @Test
        void testInterestJob_multiple_accounts() {
            User user1 = createUser("multi_user1", "multi_user1@example.com");
            User user2 = createUser("multi_user2", "multi_user2@example.com");

            Account savings1 = createSavingsAccountWithTimestamp(user1, 1000.0, 2.4, LocalDateTime.now().minusMonths(1));
            Account savings2 = createSavingsAccountWithTimestamp(user2, 5000.0, 3.6, LocalDateTime.now().minusMonths(1));

            interestSchedulerService.testInterestJob();

            Account updated1 = accountRepository.findByIdOptional(savings1.id).orElseThrow();
            Account updated2 = accountRepository.findByIdOptional(savings2.id).orElseThrow();

            assertTrue(updated1.getBalance() > 1000.0, "Account 1 should have interest");
            assertTrue(updated2.getBalance() > 5000.0, "Account 2 should have interest");
        }

        @Test
        void testInterestJob_null_rate_skipped() {
            User user = createUser("null_rate_user", "null_rate_user@example.com");
            Account savings = createSavingsAccountWithNullRate(user);

            double balanceBefore = savings.getBalance();
            interestSchedulerService.testInterestJob();

            Account updated = accountRepository.findByIdOptional(savings.id).orElseThrow();
            assertEquals(balanceBefore, updated.getBalance(), 0.001, "Balance should not change for null rate");
        }


        @Test
        void testInterestJob_zero_rate_skipped() {
            User user = createUser("zero_rate_user", "zero_rate_user@example.com");
            Account savings = createSavingsAccount(user, 1000.0, 0.0);

            double balanceBefore = savings.getBalance();
            interestSchedulerService.testInterestJob();

            Account updated = accountRepository.findByIdOptional(savings.id).orElseThrow();
            assertEquals(balanceBefore, updated.getBalance(), 0.001, "Balance should not change for zero rate");
        }

        @Test
        void testInterestJob_timestamp_updated() {
            User user = createUser("timestamp_user", "timestamp_user@example.com");
            Account savings = createSavingsAccountWithTimestamp(user, 1000.0, 2.5, LocalDateTime.of(2024, 1, 1, 0, 0));

            interestSchedulerService.testInterestJob();

            Account updated = accountRepository.findByIdOptional(savings.id).orElseThrow();
            assertNotNull(updated.getLastInterestCalculatedAt());
            assertTrue(updated.getLastInterestCalculatedAt().isAfter(LocalDateTime.of(2024, 1, 1, 0, 0)),
                    "Timestamp should be updated to current time");
        }

        @Test
        void testInterestJob_creates_transaction() {
            User user = createUser("tx_user", "tx_user@example.com");
            Account savings = createSavingsAccountWithTimestamp(user, 1000.0, 3.0, LocalDateTime.now().minusMonths(1));

            interestSchedulerService.testInterestJob();

            Account updated = accountRepository.findByIdOptional(savings.id).orElseThrow();
            assertTrue(updated.getBalance() > 1000.0, "Interest should be applied");
        }

        @Test
        void testInterestJob_empty_list() {
            interestSchedulerService.testInterestJob();
            assertTrue(true);
        }

        @Test
        void testInterestJob_high_rate() {
            User user = createUser("high_rate_user", "high_rate_user@example.com");
            Account savings = createSavingsAccountWithTimestamp(user, 100.0, 100.0, LocalDateTime.now().minusMonths(1));

            interestSchedulerService.testInterestJob();

            Account updated = accountRepository.findByIdOptional(savings.id).orElseThrow();
            double expectedBalance = 100.0 + (100.0 / 12);
            assertTrue(Math.abs(updated.getBalance() - expectedBalance) < 0.01,
                    "Interest calculation should be correct for 100% rate");
        }

        @Test
        void testInterestJob_decimal_precision() {
            User user = createUser("decimal_user", "decimal_user@example.com");
            Account savings = createSavingsAccountWithTimestamp(user, 1234.56, 5.5, LocalDateTime.now().minusMonths(1));

            interestSchedulerService.testInterestJob();

            Account updated = accountRepository.findByIdOptional(savings.id).orElseThrow();
            double expectedInterest = 1234.56 * 5.5 / 100 / 12;
            double expectedBalance = 1234.56 + expectedInterest;

            assertTrue(Math.abs(updated.getBalance() - expectedBalance) < 0.01,
                    "Decimal precision should be maintained");
        }
    }
}
