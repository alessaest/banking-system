package com.bank.service;

import com.bank.entity.Account;
import com.bank.entity.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    AccountRepository accountRepository;

    @Inject
    AccountService accountService;

    // Transfer money between accounts
    @Transactional
    public Transaction transferMoney(Long fromAccountId, Long toAccountId, Double amount, String description) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than 0");
        }
        if (fromAccountId.equals(toAccountId) ) {
            throw new IllegalArgumentException("Transfer from the same account is not applicable");
        }

        Account fromAccount = accountService.getAccountById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        Account toAccount = accountService.getAccountById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (fromAccount.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance for transfer");
        }

        // Deduct from source account
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        accountRepository.persist(fromAccount);

        // Add to destination account
        toAccount.setBalance(toAccount.getBalance() + amount);
        accountRepository.persist(toAccount);

        // Create transaction record
        Transaction tx = new Transaction();
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        tx.setAmount(amount);
        tx.setType("TRANSFER");
        tx.setStatus("Completed");
        tx.setDescription(description != null ? description : "");

        transactionRepository.persist(tx);
        return tx;
    }

    // Deposit transaction (money into account)
    @Transactional
    public Transaction createDepositTransaction(Long accountId, Double amount, String description) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than 0");
        }

        Account account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.setBalance(account.getBalance() + amount);
        accountRepository.persist(account);

        Transaction tx = new Transaction();
        tx.setToAccount(account);
        tx.setAmount(amount);
        tx.setType("DEPOSIT");
        tx.setStatus("Completed");
        tx.setDescription(description != null ? description : "Deposit");

        transactionRepository.persist(tx);
        return tx;
    }

    @Transactional
    public Transaction createWithdrawalTransaction(Long accountId, Double amount, String description) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than 0");
        }

        Account account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.setBalance(account.getBalance() - amount);
        accountRepository.persist(account);

        Transaction tx = new Transaction();
        tx.setFromAccount(account);
        tx.setAmount(amount);
        tx.setType("WITHDRAWAL");
        tx.setStatus("Completed");
        tx.setDescription(description != null ? description : "Deposit");

        transactionRepository.persist(tx);
        return tx;
    }



    // Get transaction history for an account
    public List<Transaction> getAccountTransactionHistory(Long accountId) {
        return transactionRepository.getAccountTransactions(accountId);
    }

    // Get all transactions for a user (all their accounts)
    public List<Transaction> getUserTransactionHistory(Long userId) {
        return transactionRepository.getUserTransactions(userId);
    }

    // Get transaction by ID
    public Optional<Transaction> getTransactionById(Long transactionId) {
        return transactionRepository.findByIdOptional(transactionId);
    }

    // Get transactions by type (TRANSFER, DEPOSIT, WITHDRAWAL)
    public List<Transaction> getTransactionsByType(String type) {
        return transactionRepository.find("type", type).list();
    }
}
