package com.bank.service;

import com.bank.entity.Account;
import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
    public Account createAccount(Long userId, String accountType, Double initialBalance) {
        if (accountType == null || (!accountType.equalsIgnoreCase("DEBIT") && !accountType.equalsIgnoreCase("CREDIT"))) {
            throw new IllegalArgumentException("Invalid account type. Must be 'DEBIT' or 'CREDIT'");
        }

        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(accountType.toUpperCase()); // "DEBIT" or "CREDIT"
        account.setBalance(initialBalance != null ? initialBalance : 0.0);
        account.setUser(user);

        accountRepository.persist(account);
        return account;
    }

    // Get all accounts for a user
    public List<Account> getUserAccounts(Long userId) {
        return accountRepository.find("user.id", userId).list();
    }

    // Get account by account number
    public Optional<Account> getAccountByNumber(String accountNumber) {
        return Optional.ofNullable(accountRepository.findByAccountNumber(accountNumber));
    }

    // Get account by ID
    public Optional<Account> getAccountById(Long accountId) {
        return accountRepository.findByIdOptional(accountId);
    }

    // Get account balance
    public Double getAccountBalance(Long accountId) {
        return getAccountById(accountId)
                .map(Account::getBalance)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    // Deposit money into an account
    @Transactional
    public Account deposit(Long accountId, Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than 0");
        }

        Account account = getAccountById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.setBalance(account.getBalance() + amount);
        accountRepository.persist(account);

        Transaction tx = new Transaction();
        tx.setToAccount(account);
        tx.setAmount(amount);
        tx.setStatus("COMPLETED");
        tx.setType("DEPOSIT");
        tx.setDescription("Deposit");
        transactionRepository.persist(tx);

        return account;
    }

    // Withdraw money from an account
    @Transactional
    public Account withdraw(Long accountId, Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than 0");
        }

        Account account = getAccountById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.persist(account);

        Transaction tx = new Transaction();
        tx.setToAccount(account);
        tx.setAmount(amount);
        tx.setStatus("COMPLETED");
        tx.setType("WITHDRAWAL");
        tx.setDescription("Withdrawal");
        transactionRepository.persist(tx);

        return account;
    }

    // Delete account
    @Transactional
    public void deleteAccount(Long accountId) {
        accountRepository.deleteById(accountId);
    }

    // Generate unique account number
    private String generateAccountNumber() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
    }
}