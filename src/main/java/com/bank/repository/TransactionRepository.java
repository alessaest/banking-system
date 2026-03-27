package com.bank.repository;

import com.bank.entity.Transaction;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;


//handles the database queries related to transactions
@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

    public List<Transaction> getTransactionsByUserId(Long userId) {
        return list(
                "userId = ?1 order by dateTime desc",
                userId
        );
    }

    public List<Transaction> getAccountTransactions(Long accountId) {
        return list(
                "fromAccount.id = ?1 or toAccount.id = ?1 order by dateTime desc",
                accountId
        );
    }

    //1.1.0
    public List<Transaction> getAccountTransactionsForUser(Long accountId, Long userId) {
        return list("(fromAccount.id = ?1 or toAccount.id = ?1) and userId = ?2 order by dateTime desc", accountId, userId);
    }

    public List<Transaction> getAccountTransactionsByType(
            Long accountId, Long userId, String type) {
        return list(
                "(fromAccount.id = ?1 or toAccount.id = ?1) " +
                        "and userId = ?2 " +
                        "and type = ?3 " +
                        "order by dateTime desc",
                accountId, userId, type.toUpperCase()
        );
    }

    //for admin use
    public List<Transaction> getUserTransactions(Long userId) {
        return list("from Transaction t where  t.fromAccount.user.id = ?1 or t.toAccount.user.id = ?1 order by t.dateTime desc", userId);
    }

    public Optional<Transaction> findTransactionsById(Long transactionId) {
        return findByIdOptional(transactionId);
    }

    public List<Transaction> getTransactionsByUserIdPaged(Long userId, int pageNumber, int pageSize) {
        return find("userId = ?1 order by dateTime desc", userId)
                .page(pageNumber, pageSize)
                .list();
    }

    public List<Transaction> getAccountTransactionsForUserPaged(
            Long accountId, Long userId, int pageNumber, int pageSize) {
        return find(
                "(fromAccount.id = ?1 or toAccount.id = ?1) and userId = ?2 order by dateTime desc",
                accountId, userId
        ).page(pageNumber, pageSize).list();
    }

    public long countByUserId(Long userId) {
        return count("userId = ?1", userId);
    }

    public long countAccountTransactionsForUser(Long accountId, Long userId) {
        return count(
                "(fromAccount.id = ?1 or toAccount.id = ?1) and userId = ?2",
                accountId, userId
        );
    }
}