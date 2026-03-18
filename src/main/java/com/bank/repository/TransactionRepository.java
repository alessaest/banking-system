package com.bank.repository;

import com.bank.entity.Transaction;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

    public List<Transaction> getAccountTransactions(Long accountId) {
        return list(
                "fromAccount.id = ?1 or toAccount.id = ?1 order by dateTime desc",
                accountId
        );
    }

    public List<Transaction> getUserTransactions(Long userId) {
        return list("from Transaction t where  t.fromAccount.user.id = ?1 or t.toAccount.user.id = ?1 order by t.dateTime desc", userId);
    }

    public Optional<Transaction> findTransactionsById(Long transactionId) {
        return findByIdOptional(transactionId);
    }

    public List<Transaction> getTransactionsWithPagination (Long accountId, int pageNumber, int pageSize){
        return find("fromAccount.id = ?1 or toAccount.id = ?1 order by dateTime desc", accountId)
                .page(pageNumber, pageSize)
                .list();
    }

    public long countAccountTransactions(Long accountId) {
        return count("fromAccount.id = ?1 or toAccount.id = ?1", accountId);
    }

}