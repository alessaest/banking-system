package com.bank.service;

import com.bank.dto.DTORequest;
import com.bank.entity.Account;
import com.bank.entity.Transaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;


//applies the business rules for transaction management
@ApplicationScoped
public class TransactionService {

    private static final Logger logger = Logger.getLogger(TransactionService.class);

    //constants
    private static final String ACCOUNT_NOT_FOUND = "Account not found";

    private static final String STATUS_COMPLETED = "Completed";
    private static final String INSUFFICIENT_BALANCE = "Insufficient balance";

    private static final String TYPE_TRANSFER = "TRANSFER";
    private static final String TYPE_DEPOSIT = "DEPOSIT";
    private static final String TYPE_WITHDRAW = "WITHDRAWAL";
    private static final String TYPE_INTEREST = "INTEREST";


    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    TransactionService (TransactionRepository transactionRepository, AccountRepository accountRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    // Transfer money between accounts
    @Transactional
    public DTORequest.TransactionResponse transferMoney(DTORequest.TransferRequest request, Long requestingUserId) {
        if (request.getAmount() == null || request.getAmount() <= 0)
            throw new IllegalArgumentException("Transfer amount must be greater than 0");
        if (request.getFromAccountId().equals(request.getToAccountId()))
            throw new IllegalArgumentException("Cannot transfer to the same account");

        Account fromAccount = accountRepository.findByIdOptional(request.getFromAccountId()).orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));

        if (!fromAccount.getUser().id.equals(requestingUserId))
            throw new IllegalArgumentException("Can only transfer from your account");

        Account toAccount = accountRepository.findByIdOptional(request.getToAccountId()).orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));

        if (fromAccount.getBalance() < request.getAmount())
            throw new IllegalArgumentException(INSUFFICIENT_BALANCE);

        fromAccount.setBalance(fromAccount.getBalance() - request.getAmount());
        accountRepository.persist(fromAccount);

        toAccount.setBalance(toAccount.getBalance() + request.getAmount());
        accountRepository.persist(toAccount);

        Transaction tx = new Transaction(
                fromAccount, toAccount, requestingUserId, request.getAmount(), TYPE_TRANSFER, STATUS_COMPLETED, request.getDescription() != null ? request.getDescription() : ""
        );
        transactionRepository.persist(tx);

        return accountService.toTransactionResponse(tx);
    }

    /**
     * Deposits money to an account. This method automatically detects the account type.
     *
     * @deprecated Since 2.2.0 - Use type-specific deposit methods instead:
     *             <ul>
     *             <li>{@link #depositToOwnAccount(DTORequest.DepositRequest, Long)} - For user self-deposits</li>
     *             <li>{@link #depositToEmployeeAccount(Long, Double, Long)} - For admin payroll deposits</li>
     *             </ul>
     *             This method will be removed in version 2.5.0 (???? 2026).
     *
     * @param request The deposit request containing account ID and amount
     * @param requestingUserId The user performing the deposit
     * @return Transaction response
     */
    @Deprecated(since = "2.2.0", forRemoval = true)
    @Transactional
    public DTORequest.TransactionResponse deposit(DTORequest.DepositRequest request, Long requestingUserId) {

        logger.infof("[DEPRECATED] deposit() called at " + java.time.LocalDateTime.now() +
                " - User: " + requestingUserId + " - Account: " + request.getAccountId());

        Account account = accountRepository.findByIdOptional(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));

        if (account.isDebit()) {
            return accountService.depositToDebit(request.getAccountId(), request.getAmount(), requestingUserId, false);
        } else if (account.isCredit()) {
            return accountService.depositToCredit(request.getAccountId(), request.getAmount(), requestingUserId, false);
        } else {
            throw new IllegalArgumentException("Invalid account type");
        }
    }

    // new method
    @Transactional
    public DTORequest.TransactionResponse depositToOwnAccount(
            DTORequest.DepositRequest request,
            Long requestingUserId) {
        Account account = accountRepository.findByIdOptional(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));

        if (!account.getUser().id.equals(requestingUserId)) {
            throw new IllegalArgumentException("Can only deposit from your account");
        }

        return performDeposit(account, request.getAmount(), requestingUserId, "USER_DEPOSIT");
    }
    // new method
    public DTORequest.TransactionResponse depositToEmployeeAccount(
            Long employeeAccountId, Double amount, Long adminUserId) {

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than 0");
        }

        Account account = accountRepository.findByIdOptional(employeeAccountId).orElseThrow(() -> new IllegalArgumentException("Account does not exist"));

        return performDeposit(account, amount, adminUserId, "ADMIN_PAYROLL_DEPOSIT");
    }

    // Withdraw money from user's account
    @Transactional
    public DTORequest.TransactionResponse performWithdraw(DTORequest.WithdrawRequest request, Long requestingUserId) {
        if (request.getAmount() == null || request.getAmount() <= 0)
            throw new IllegalArgumentException("Withdraw amount must be greater than 0");

        Account account = accountRepository.findByIdOptional(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));

        if (!account.getUser().id.equals(requestingUserId))
            throw new IllegalArgumentException("Can only withdraw from your own account");

        // Check balance for all account types
        if (account.getBalance() < request.getAmount())
            throw new IllegalArgumentException(INSUFFICIENT_BALANCE);

        // Deduct the amount
        account.setBalance(account.getBalance() - request.getAmount());
        accountRepository.persist(account);

        // Create transaction record
        Transaction tx = new Transaction(
                account, null, requestingUserId, request.getAmount(), TYPE_WITHDRAW, STATUS_COMPLETED, "Withdrawal"
        );
        transactionRepository.persist(tx);

        return accountService.toTransactionResponse(tx);
    }


    private DTORequest.TransactionResponse performDeposit(Account account, Double amount, Long userId, String depositType) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than 0");
        }

        logger.infof("[" + depositType + "] User: " + userId + " - Account: " + account.id + " - Amount: " + amount + " - Timestamp: " + java.time.LocalDateTime.now());

        // Determine if this is an admin deposit based on the type
        boolean isAdminDeposit = depositType.equals("ADMIN_PAYROLL_DEPOSIT");

        if (account.isDebit()) {
            return accountService.depositToDebit(account.id, amount, userId, isAdminDeposit);
        } else if (account.isCredit()) {
            return accountService.depositToCredit(account.id, amount, userId, isAdminDeposit);
        } else if (account.isSavings()) {
            return accountService.depositToSavings(account.id, amount, userId, isAdminDeposit);
        } else {
            throw new IllegalArgumentException("Invalid account type");
        }
    }

    //1.1.0
    // transaction history based on the account
    @Transactional
    public List<DTORequest.TransactionResponse> getAccountTransactionHistory(Long accountId, Long requestingUserId) {

        Account account = accountRepository.findByIdOptional(accountId)
                .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));
        if (!account.getUser().id.equals(requestingUserId))
            throw new IllegalArgumentException("Can only view your account history");

        return transactionRepository
                .getAccountTransactionsForUser(accountId, requestingUserId)
                .stream()
                .map(accountService::toTransactionResponse)
                .toList();
    }

    // transaction type filter
    @Transactional
    public List<DTORequest.TransactionResponse> getAccountTransactionHistoryByType(
            Long accountId, Long requestingUserId, String type) {

        Account account = accountRepository.findByIdOptional(accountId)
                .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));

        if (!account.getUser().id.equals(requestingUserId))
            throw new IllegalArgumentException("Can only view your account history");

        String validType = type.toUpperCase();
        if (!validType.equals(TYPE_DEPOSIT) &&
            !validType.equals(TYPE_WITHDRAW) &&
            !validType.equals(TYPE_TRANSFER) &&
            !validType.equals(TYPE_INTEREST))
            throw new IllegalArgumentException("Invalid type filter. Use DEPOSIT, WITHDRAWAL, or TRANSFER.");

        return transactionRepository
                .getAccountTransactionsByType(accountId, requestingUserId, validType)
                .stream()
                .map(accountService::toTransactionResponse)
                .toList();
    }

    public Optional<DTORequest.TransactionResponse> getTransactionById (Long transactionId){
        return transactionRepository.findTransactionsById(transactionId)
                .map(accountService::toTransactionResponse);
    }
}