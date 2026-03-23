package com.bank.service;

import com.bank.dto.DTORequest;
import com.bank.entity.Account;
import com.bank.entity.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;


//applies the business rules for transaction management
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
    public DTORequest.TransactionResponse transferMoney(DTORequest.TransferRequest request, Long requestingUserId) {
        if (request.getAmount() == null || request.getAmount() <= 0)
            throw new IllegalArgumentException("Transfer amount must be greater than 0");
        if (request.getFromAccountId().equals(request.getToAccountId()))
            throw new IllegalArgumentException("Cannot transfer to the same account");

        Account fromAccount = accountRepository.findByIdOptional(request.getFromAccountId()).orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!fromAccount.getUser().id.equals(requestingUserId))
            throw new IllegalArgumentException("Can only transfer from your account");

        Account toAccount = accountRepository.findByIdOptional(request.getToAccountId()).orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (fromAccount.getBalance() < request.getAmount())
            throw new IllegalArgumentException("Insufficient balance");

        fromAccount.setBalance(fromAccount.getBalance() - request.getAmount());
        accountRepository.persist(fromAccount);

        toAccount.setBalance(toAccount.getBalance() + request.getAmount());
        accountRepository.persist(toAccount);

        Transaction tx = new Transaction(
                fromAccount, toAccount, request.getAmount(), "TRANSFER", "Completed", request.getDescription() != null ? request.getDescription() : ""
        );
        transactionRepository.persist(tx);

        return accountService.toTransactionResponse(tx);
    }

    @Transactional
    public DTORequest.TransactionResponse deposit(DTORequest.DepositRequest request, Long requestingUserId) {
        return accountService.deposit(request.getAccountId(), request.getAmount(), requestingUserId);
    }


    @Transactional
    public List<DTORequest.TransactionResponse> getAccountTransactionHistory(Long accountId, Long requestingUserId) {
        Account account = accountRepository.findByIdOptional(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!account.getUser().id.equals(requestingUserId))
            throw new IllegalArgumentException("Can only view your account history");

        return transactionRepository.getAccountTransactions(accountId)
                .stream()
                .map(accountService::toTransactionResponse)
                .toList();
    }

    public Optional<DTORequest.TransactionResponse> getTransactionById (Long transactionId){
        return transactionRepository.findTransactionsById(transactionId)
                .map(accountService::toTransactionResponse);
    }
}