package com.bank.repository;

import com.bank.entity.Account;
import com.bank.entity.Transaction;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

    public List<Transaction> findTransactionsByAccount(Long accountId) {
        return list(
                "fromAccount = ?1 or toAccount = ?1 order by dateTime desc",
                accountId
        );
    }

    public List<Transaction> findSentTransactions(Account account) {
        return list("fromAccount = ?1 order by dateTime desc", account);
    }

    public List<Transaction> findSentTransactionsById(Long accountId) {
        return list("fromAccount.id = ?1 order by dateTime desc", accountId);
    }

    public List<Transaction> findReceivedTransactions(Account account) {
        return list("toAccount = ?1 order by dateTime desc", account);
    }

    public List<Transaction> findReceivedTransactionsById(Long accountId) {
        return list("toAccount.id = ?1 order by dateTime desc", accountId);
    }

    public List<Transaction> findByAccountAndType(Account account, String type) {
        return list(
                "(fromAccount = ?1 or toAccount = ?1) and type = ?2 order by dateTime desc",
                account, type
        );
    }

    public List<Transaction> findByTypeAndStatus(String type, String status) {
        return list("type = ?1 and status = ?2 order by dateTime desc", type, status);
    }

    public List<Transaction> findTransactionsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return list("dateTime between ?1 and ?2 order by dateTime desc", startDate, endDate);
    }

    public List<Transaction> findTransactionsByAccountAndDates(Account account, LocalDateTime startDate, LocalDateTime endDate) {
        return list("(fromAccount = ?1 or toAccount = ?1) and dateTime between ?2 and ?3 order by dateTime desc", account, startDate, endDate);
    }

    public List<Transaction> findByType(String type) {
        return find("type", type).list();
    }

    public List<Transaction> findByStatus(String status) {
        return find("status", status).list();
    }

    public List<Transaction> findByAccountAndStatus(Long accountId, String status) {
        return list("(fromAccount.id = ?1 or toAccount.id = ?1) and status = ?2 order by dateTime desc", accountId, status);
    }

    public long countCompletedTransactions(Long accountId) {
        return count("(fromAccount.id = ?1 or toAccount.id = ?1) and status = ?2", accountId, "Completed");
    }

    public long countFailedTransactions(Long accountId) {
        return count("(fromAccount.id = ?1 or toAccount.id = ?1) and status = ?2", accountId, "Failed");
    }

    public long countTransactionsByType(Long accountId, String type) {
        return count("(fromAccount.id = ?1 or toAccount.id = ?1) and type = ?2", accountId, type);
    }

    public Double sumTransferredAmount(Long accountId) {
        Object result = getEntityManager()
                .createQuery("select sum(t.amount) from Transaction t where t.fromAccount.id = ?1 and t.type = 'TRANSFER'")
                .setParameter(1, accountId).getSingleResult();
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    public Double sumReceivedAmount(Long accountId) {
        Object result = getEntityManager()
                .createQuery("select sum(t.amount) from Transaction t where t.toAccount.id = ?1 and t.type = 'TRANSFER'")
                .setParameter(1, accountId).getSingleResult();
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    public List<Transaction> findLatestTransactions(Long account, int limit) {
        return find("fromAccount.id = ?1 or toAccount.id = ?1 order by dateTime desc", account)
                .page(0, limit)
                .list();
    }

    public List<Transaction> findLatestTransactionsById(Long accountId, int limit) {
        return find("fromAccount.id = ?1 or toAccount.id = ?1 order by dateTime desc", accountId)
                .page(0, limit)
                .list();
    }

    public List<Transaction> getTransactionsWithPagination(Long accountId, int pageNumber, int pageSize) {
        return find("fromAccount.id = ?1 or toAccount.id = ?1 order by dateTime desc", accountId)
                .page(pageNumber, pageSize)
                .list();
    }

    public List<Transaction> getAccountTransactions(Long accountId) {
        return list("fromAccount.id = ?1 or toAccount.id = ?1 order by dateTime desc", accountId);
    }

    public List<Transaction> getUserTransactions(Long userId) {
        return list(
                "from Transaction t where t.fromAccount.user.id = ?1 or t.toAccount.user.id = ?1 order by t.dateTime desc",
                userId
        );
    }

    public long countUserTransactions(Long userId) {
        return count("fromAccount.user.id = ?1 or toAccount.user.id = ?1", userId);
    }

    public long countAccountTransactions(Long accountId) {
        return count("fromAccount.id = ?1 or toAccount.id = ?1", accountId);
    }

    public long getTotalTransactionCount() {
        return count();
    }

    public List<Transaction> findTransactionsBetweenAccounts(Long fromAccountId, Long toAccountId) {
        return list(
                "(fromAccount.id = ?1 and toAccount.id = ?2) or (fromAccount.id = ?2 and toAccount.id = ?1) order by dateTime desc",
                fromAccountId, toAccountId
        );
    }

    public List<Transaction> getUserTransactionsWithPagination(Long userId, int pageNumber, int pageSize) {
        return find("from Transaction t where t.fromAccount.user.id = ?1 or t.toAccount.user.id = ?1 order by t.dateTime desc", userId)
                .page(pageNumber, pageSize)
                .list();
    }

    public List<Transaction> getRecentTransactions(Long accountId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return list(
                "(fromAccount.id = ?1 or toAccount.id = ?1) and dateTime >= ?2 order by dateTime desc",
                accountId, startDate
        );
    }

    public List<Transaction> getHighValueTransactions(Long accountId, Double threshold) {
        return list(
                "(fromAccount.id = ?1 or toAccount.id = ?1) and amount >= ?2 order by amount desc",
                accountId, threshold
        );
    }

//    public List<Transaction> findByAccountAndMultipleTypes(Long accountId, List<String> types) {
//        String typesList = String.join("','", types);
//        return list(
//                "(fromAccount.id = ?1 or toAccount.id = ?1) and type in ('" + typesList + "') order by dateTime desc",
//                accountId
//        );
//    }

    public boolean transactionExists(Long transactionId) {
        return findByIdOptional(transactionId).isPresent();
    }

    public Optional<Transaction> findTransactionById(Long transactionId) {
        return findByIdOptional(transactionId);
    }

    public Double getAverageTransactionAmount(Long accountId) {
        Object result = getEntityManager()
                .createQuery("select avg(t. amount) from Transaction t where t.fromAccount.id = ?1 or t.toAccount.id = ?1")
                .setParameter(1, accountId).getSingleResult();
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    public Double getMaxTransactionAmount(Long accountId) {
        Object result = getEntityManager()
                .createQuery("select max(t.amount) from Transaction t where t.fromAccount.id = ?1 or t.toAccount.id = ?1")
                .setParameter(1, accountId).getSingleResult();
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    public Double getMinTransactionAmount(Long accountId) {
        Object result = getEntityManager()
                .createQuery("select min(t.amount) from Transaction t where t.fromAccount.id = ?1 or t.toAccount.id = ?1")
                .setParameter(1, accountId).getSingleResult();
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }
}
