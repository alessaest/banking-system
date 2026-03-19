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
            accountRepository.persist(debit);
            created.add(debit);
        }

        if (type.equals("CREDIT") || type.equals("BOTH")) {
            if (accountRepository.userHasAccountType(user.id, "CREDIT")) {
                throw new IllegalArgumentException("User already has a CREDIT account");
            }

            Account credit = new Account(generateAccountNumber(), 0.0, "CREDIT", user);
            accountRepository.persist(credit);
            created.add(credit);
        }

        return created;
    }

    // Get all accounts for a user
    public List<Account> getMyAccounts(Long userId) {
        return accountRepository.findByUserId(userId);
    }


    // Get account by ID
    public Optional<Account> getAccountById(Long accountId) {
        return accountRepository.findByIdOptional(accountId);
    }


    @Transactional
    public DTORequest.TransactionResponse deposit(Long accountId, Double amount, Long requestingUserId) {
        if (amount == null || amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Account account = accountRepository.findByIdOptional(accountId).orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        if (!account.getUser().id.equals(requestingUserId))
            throw new IllegalArgumentException("User does not own this account");

        if (account.isCredit())
            throw new IllegalArgumentException("Credit account cannot receive deposits.");

        account.setBalance(account.getBalance() + amount);
        accountRepository.persist(account);

        Transaction tx = new Transaction(null, account, amount, "DEPOSIT", "Completed", "Deposit");
        transactionRepository.persist(tx);
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

        if (account.getBalance() < amount)
            throw new IllegalArgumentException("Insufficient balance.");

        account.setBalance(account.getBalance() - amount);
        accountRepository.persist(account);

        Transaction tx = new Transaction(account, null, amount, "WITHDRAWAL", "Completed", "Withdrawal");
        transactionRepository.persist(tx);
        return toTransactionResponse(tx);
    }

    @Transactional
    public DTORequest.AccountResponse updateCreditBalance(Long accountId, Double newBalance) {

        if (newBalance == null || newBalance <= 0)
            throw new IllegalArgumentException("New balance must be positive");

        Account account = accountRepository.findByIdOptional(accountId).orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        if (!account.isCredit())
            throw new IllegalArgumentException("Credit account balances can be updated by admin");

        account.setBalance(newBalance);
        accountRepository.persist(account);

        return toAccountResponse(account);
    }

    // Delete account
    @Transactional
    public void deleteAccount(Long accountId) {
        accountRepository.findByIdOptional(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));

        jakarta.persistence.EntityManager em = accountRepository.getEntityManager();

        // Step 1 — delete linked transactions first
        em.createNativeQuery(
                "DELETE FROM transaction WHERE from_account_id = :aid OR to_account_id = :aid"
        ).setParameter("aid", accountId).executeUpdate();

        // Step 2 — flush and clear
        em.flush();
        em.clear();

        // Step 3 — delete account
        em.createNativeQuery(
                "DELETE FROM account WHERE id = :aid"
        ).setParameter("aid", accountId).executeUpdate();
    }

    public Double getAccountBalance(Long accountId, Long requestUserId) {
        Account account = accountRepository.findByIdOptional(accountId).orElseThrow(() -> new IllegalArgumentException("Account does not exist"));
        if (!account.getUser().id.equals(requestUserId))
            throw new IllegalArgumentException("You can only view your own account balance");
        return account.getBalance();
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
                tx.getAmount(),
                tx.getType(),
                tx.getStatus(),
                tx.getDescription(),
                tx.getDateTime()
        );
    }

    // Generate unique account number
    private String generateAccountNumber() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16).toUpperCase();
    }
}