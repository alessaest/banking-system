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


    // Create a new account (debit or credit)
    @Transactional
    public List<Account> createAccountForUser(User user, String accountType, Double initialDebitBalance) {
        List<Account> created = new ArrayList<>();
        String type = accountType.toUpperCase();

        if (type.equals("DEBIT") || type.equals("BOTH")) {
            if (accountRepository.userHasAccountType(user.id, "DEBIT")) {
                throw new IllegalArgumentException("User already has a DEBIT account");
            }
            Account debit = new Account(generateAccountNumber(), initialDebitBalance != null ? initialDebitBalance : 0.0, "DEBIT", user);
            debit.setCreditLimit(null);
            accountRepository.persist(debit);
            created.add(debit);
        }

        if (type.equals("CREDIT") || type.equals("BOTH")) {
            if (accountRepository.userHasAccountType(user.id, "CREDIT")) {
                throw new IllegalArgumentException("User already has a CREDIT account");
            }

            Account credit = new Account(generateAccountNumber(), 0.0, "CREDIT", user);
            credit.setCreditLimit(0.0);
            accountRepository.persist(credit);
            created.add(credit);
        }

        return created;
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
        return toTransactionResponse(tx);
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
        return toTransactionResponse(tx);
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
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.persist(account);

        Transaction tx = new Transaction(account, null, requestingUserId, amount, "WITHDRAWAL", "Completed", "Withdrawal");
        transactionRepository.persist(tx);
        DTORequest.TransactionResponse response = toTransactionResponse(tx);
        response.setAvailableBalance(account.getBalance());
        return toTransactionResponse(tx);
    }

    //admin access
    @Transactional
    public DTORequest.AccountResponse updateCreditBalance(Long accountId, Double newLimit) {

        if (newLimit == null || newLimit <= 0)
            throw new IllegalArgumentException("New balance must be positive");

        Account account = accountRepository.findByIdOptional(accountId).orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        if (!account.isCredit())
            throw new IllegalArgumentException("Credit account balances can be updated by admin");

        account.setBalance(newLimit);
        accountRepository.persist(account);
        return toAccountResponse(account);
    }

    //admin access - update the credit limit for a CREDIT account
    @Transactional
    public DTORequest.AccountResponse updateCreditLimit(Long accountId, Double newLimit) {
        if (newLimit == null || newLimit <= 0)
            throw new IllegalArgumentException("Credit limit must be positive");

        Account account = accountRepository.findByIdOptional(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        if (!account.isCredit())
            throw new IllegalArgumentException("Only CREDIT account limits can be updated");

        account.setCreditLimit(newLimit);
        account.setBalance(newLimit);
        accountRepository.persist(account);
        return toAccountResponse(account);
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
        return new DTORequest.AccountResponse(
                account.id,
                account.getUser().id,
                account.getAccountNumber(),
                account.getBalance(),
                account.getAccountType(),
                account.getCreatedAt()
        );
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