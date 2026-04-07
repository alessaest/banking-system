package com.bank.service;

import com.bank.dto.DTORequest;
import com.bank.entity.Account;
import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//applies the business rules for account management such as creating accounts, depositing, withdrawing and updating credit balance - admin only
@ApplicationScoped
public class AccountService {

    @Inject
    AccountRepository accountRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    TransactionRepository transactionRepository;


    // Create a new account (debit or credit or savings or all)
    @Transactional
    public List<Account> createAccountForUser(User user, String accountType, Double initialDebitBalance, Double initialSavingsBalance) {
        List<Account> created = new ArrayList<>();
        String type = accountType.toUpperCase();

        // Handle ALL - create all three account types
        if (type.equals("DEBIT_CREDIT")) {
            createDebitAccount(user, initialDebitBalance, created);
            createCreditAccount(user, created);
        }
        else if (type.equals("DEBIT_SAVINGS")) {
            createDebitAccount(user, initialDebitBalance, created);
            created.add(createSavingsAccount(user, initialSavingsBalance, 2.5));
        }
        else if (type.equals("CREDIT_SAVINGS")) {
            createCreditAccount(user, created);
            created.add(createSavingsAccount(user, initialSavingsBalance, 2.5));
        }
        else if (type.equals("ALL")) {
            createDebitAccount(user, initialDebitBalance, created);
            createCreditAccount(user, created);
            created.add(createSavingsAccount(user, initialSavingsBalance, 2.5));
        }
        else if (type.equals("DEBIT")) {
            createDebitAccount(user, initialDebitBalance, created);
        }
        else if (type.equals("CREDIT")) {
            createCreditAccount(user, created);
        }
        else if (type.equals("SAVINGS")) {
            created.add(createSavingsAccount(user, initialSavingsBalance, 2.5));
        }
        else {
            throw new IllegalStateException("Invalid account type. Valid options: DEBIT, CREDIT, SAVINGS, DEBIT_CREDIT, DEBIT_SAVINGS, CREDIT_SAVINGS, ALL");
        }
        return created;
    }

    @Transactional
    public void createDebitAccount (User user, Double initialBalance, List<Account> created) {
        if (accountRepository.userHasAccountType(user.id, "DEBIT")) {
            throw new IllegalStateException("User already has a debit account.");
        }

        Account debit = new Account(generateAccountNumber(), initialBalance != null ? initialBalance : 0.0, "DEBIT", user);
        debit.setCreditLimit(null);
        accountRepository.persist(debit);
        created.add(debit);
    }

    @Transactional
    public void createCreditAccount (User user, List<Account> created) {
        if (accountRepository.userHasAccountType(user.id, "CREDIT")) {
            throw new IllegalStateException("User already has a credit account.");
        }

        Account credit = new Account(generateAccountNumber(), 0.0, "CREDIT", user);
        credit.setCreditLimit(0.0);
        accountRepository.persist(credit);
        created.add(credit);
    }


    @Transactional
    public Account createSavingsAccount(User user, Double initialSavingsBalance, Double interestRate) {
        if (accountRepository.userHasAccountType(user.id, "SAVINGS")) {
            throw new IllegalArgumentException("User already has a savings account");
        }

        Account savings = new Account(
                generateAccountNumber(),
                initialSavingsBalance != null ? initialSavingsBalance : 0.0,
                "SAVINGS",
                interestRate,
                user
        );
        savings.setCreditLimit(null);
        accountRepository.persist(savings);
        return savings;
    }

    // Query
    public List<Account> getMyAccounts(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Optional<Account> getAccountById(Long accountId) {
        return accountRepository.findByIdOptional(accountId);
    }

    //1.0.1
    public Double getAccountBalance(Long accountId, Long requestUserId) {
        Account account = accountRepository.findByIdOptional(accountId).orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        if (!account.getUser().id.equals(requestUserId))
            throw new IllegalArgumentException("You can only view your own account balance");
        return account.getBalance();
    }

    //1.1.0
    @Transactional
    public DTORequest.TransactionResponse depositToDebit(Long accountId, Double amount, Long requestingUserId) {
        if (amount == null || amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Account account = accountRepository.findByIdOptional(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        if (!account.getUser().id.equals(requestingUserId))
            throw new IllegalArgumentException("User does not own this account");

        // Check that this is a DEBIT account
        if (!account.isDebit())
            throw new IllegalArgumentException("This operation is only available for DEBIT accounts");

        account.setBalance(account.getBalance() + amount);
        accountRepository.persist(account);

        Transaction tx = new Transaction(null, account, requestingUserId, amount, "DEPOSIT", "Completed", "Deposit into DEBIT account");
        transactionRepository.persist(tx);

        DTORequest.TransactionResponse response = toTransactionResponse(tx);
        response.setAvailableBalance(account.getBalance());
        return response;
    }

    @Transactional
    public DTORequest.TransactionResponse depositToCredit(Long accountId, Double amount, Long requestingUserId) {
        if (amount == null || amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Account account = accountRepository.findByIdOptional(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        if (!account.getUser().id.equals(requestingUserId))
            throw new IllegalArgumentException("User does not own this account");

        // Check that this is a CREDIT account
        if (!account.isCredit())
            throw new IllegalArgumentException("This operation is only available for CREDIT accounts");

        Double limit = account.getCreditLimit();
        if (limit == null || limit <= 0)
            throw new IllegalArgumentException("Credit account has no limit set. Please contact an admin.");

        double newBalance = account.getBalance() + amount;
        if (newBalance > limit)
            throw new IllegalArgumentException(String.format("Deposit would exceed the limit. Current balance: %.2f, Credit limit: %.2f, Attempted deposit: %.2f", account.getBalance(), limit, amount));

        account.setBalance(account.getBalance() + amount);
        accountRepository.persist(account);

        Transaction tx = new Transaction(null, account, requestingUserId, amount, "DEPOSIT", "Completed", "Deposit into CREDIT account");
        transactionRepository.persist(tx);

        DTORequest.TransactionResponse response = toTransactionResponse(tx);
        response.setAvailableBalance(account.getBalance());
        return response;
    }

    @Transactional
    public DTORequest.TransactionResponse depositToSavings(Long accountId, Double amount, Long requestingUserId) {
        if (amount == null || amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Account account = accountRepository.findByIdOptional(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        if (!account.getUser().id.equals(requestingUserId))
            throw new IllegalArgumentException("User does not own this account");

        // Check that this is a SAVINGS account
        if (!account.isSavings())
            throw new IllegalArgumentException("This operation is only available for SAVINGS accounts");

        account.setBalance(account.getBalance() + amount);
        accountRepository.persist(account);

        Transaction tx = new Transaction(null, account, requestingUserId, amount, "DEPOSIT", "Completed", "Deposit into SAVINGS account");
        transactionRepository.persist(tx);

        DTORequest.TransactionResponse response = toTransactionResponse(tx);
        response.setAvailableBalance(account.getBalance());
        return response;
    }

    // Withdraw money from an account
    @Transactional
    public DTORequest.TransactionResponse withdraw(Long accountId, Double amount, Long requestingUserId) {
        if (amount == null || amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Account account = accountRepository.findByIdOptional(accountId).orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        if (!account.getUser().id.equals(requestingUserId))
            throw new IllegalArgumentException("User does not own this account");

        if (account.isDebit()) {
            if (account.getBalance() < amount)
                throw new IllegalArgumentException("Insufficient balance.");
        } else if (account.isCredit()) {
            if (account.getBalance() < amount)
                throw new IllegalArgumentException("Insufficient credit balance.");
        } else if (account.isSavings()) {
            if (account.getBalance() < amount)
                throw new IllegalArgumentException("Insufficient savings balance.");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.persist(account);

        Transaction tx = new Transaction(account, null, requestingUserId, amount, "WITHDRAWAL", "Completed", "Withdrawal");
        transactionRepository.persist(tx);
        DTORequest.TransactionResponse response = toTransactionResponse(tx);
        response.setAvailableBalance(account.getBalance());
        return response;
    }

    //admin access
    @Transactional
    public DTORequest.AccountResponse updateCreditBalance(Long accountId, Double amountToAdd) {
        return updateCreditBothLimitAndBalance(accountId, amountToAdd);
    }

    //admin access - update the credit limit for a CREDIT account
    @Transactional
    public DTORequest.AccountResponse updateCreditLimit(Long accountId, Double amountToAdd) {
        return updateCreditBothLimitAndBalance(accountId, amountToAdd);
    }

    @Transactional
    public DTORequest.AccountResponse updateCreditBothLimitAndBalance(Long accountId, Double amountToAdd) {
        if (amountToAdd == null || amountToAdd <= 0)
            throw new IllegalArgumentException("Value must be positive");

        Account account = accountRepository.findByIdOptional(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        if (!account.isCredit())
            throw new IllegalArgumentException("Only CREDIT accounts can be updated");

        Double currentLimit = account.getCreditLimit() != null ? account.getCreditLimit() : 0.0;
        Double totalLimit = currentLimit + amountToAdd;

        account.setBalance(totalLimit);
        account.setCreditLimit(totalLimit);
        accountRepository.persist(account);

        return toAccountResponse(account);
    }

    @Transactional
    public DTORequest.AccountResponse updateSavingsInterestRate(Long accountId, Double rate) {
        if (rate == null || rate <= 0 || rate > 100) {
            throw new IllegalArgumentException("Interest rate must be between 0 and 100");
        }

        Account account = accountRepository.findByIdOptional(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        if (!account.isSavings()) {
            throw new IllegalArgumentException("Only SAVINGS accounts can have interest rates");
        }

        account.setInterestRate(rate);
        accountRepository.persist(account);

        return toAccountResponse(account);
    }


    @Transactional
    public void applyMonthlyInterestToAllSavings() {
        List<Account> savingsAccounts = accountRepository.find("accountType", "SAVINGS").list();
        int count = 0;

        for (Account account : savingsAccounts) {
            try {
                if (account.getInterestRate() != null && account.getInterestRate() > 0) {
                    // Calculate monthly interest: (balance * rate / 100) / 12
                    BigDecimal balance = new BigDecimal(account.getBalance());
                    BigDecimal rate = new BigDecimal(account.getInterestRate());
                    BigDecimal monthlyInterest = balance
                            .multiply(rate)
                            .divide(new BigDecimal(100), 10, RoundingMode.HALF_UP)
                            .divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);

                    Double interest = monthlyInterest.doubleValue();

                    // Update account balance
                    account.setBalance(account.getBalance() + interest);
                    account.setLastInterestCalculatedAt(LocalDateTime.now());
                    accountRepository.persist(account);

                    // Record as transaction
                    Transaction interestTx = new Transaction(
                            null,
                            account,
                            account.getUser().id,
                            interest,
                            "INTEREST",
                            "Completed",
                            String.format("Monthly interest (%.2f%% annual)", account.getInterestRate())
                    );
                    transactionRepository.persist(interestTx);
                    count++;

                    System.out.println("✓ Interest applied to " + account.getAccountNumber() +
                            " | User: " + account.getUser().getUsername() +
                            " | Interest: +" + String.format("%.2f", monthlyInterest) +
                            " | New Balance: " + String.format("%.2f", account.getBalance()));
                }
            } catch (Exception e) {
                System.err.println("✗ Error applying interest to account " + account.getAccountNumber() + ": " + e.getMessage());
            }
        }
        System.out.println("Interest applied to " + count + " savings accounts");
    }

    // Delete account
    @Transactional
    public void deleteAccount(Long accountId) {
        accountRepository.findByIdOptional(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));

        jakarta.persistence.EntityManager em = accountRepository.getEntityManager();

        em.createNativeQuery(
                "DELETE FROM transaction WHERE from_account_id = :aid OR to_account_id = :aid"
        ).setParameter("aid", accountId).executeUpdate();

        em.flush();
        em.clear();

        em.createNativeQuery(
                "DELETE FROM account WHERE id = :aid"
        ).setParameter("aid", accountId).executeUpdate();
    }

    public DTORequest.AccountResponse toAccountResponse(Account account) {
        DTORequest.AccountResponse response = new DTORequest.AccountResponse(
                account.id,
                account.getUser().id,
                account.getAccountNumber(),
                account.getBalance(),
                account.getAccountType(),
                account.getCreatedAt()
        );
        response.setCreditLimit(account.getCreditLimit());
        response.setInterestRate(account.getInterestRate());
        return response;
    }

    public DTORequest.TransactionResponse toTransactionResponse(Transaction tx) {
        return new DTORequest.TransactionResponse(
                tx.id,
                tx.getFromAccount() != null ? tx.getFromAccount().id : null,
                tx.getToAccount() != null ? tx.getToAccount().id : null,
                tx.getUserId(),
                tx.getAmount(),
                tx.getType(),
                tx.getStatus(),
                tx.getDescription(),
                tx.getDateTime(),
                null
        );
    }

    // Generate unique account number
    private String generateAccountNumber() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16).toUpperCase();
    }
}
