package com.bank.repository;

import com.bank.entity.Account;
import com.bank.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class AccountRepository implements PanacheRepository<Account> {
    public Account findByAccountNumber(String accountNumber) {
        return find("accountNumber", accountNumber).firstResult();
    }

    public List<Account> findByUser(User user) {
        return list("user", user);
    }

    public List<Account> findByUserId(Long userId) {
        return list("user.id", userId);
    }

    public List<Account> findByAccountType(User user, String accountType) {
        return list("user = ?1 and accountType = ?2", user, accountType);
    }

    public List<Account> findByUserIdandType(Long userId, String accountType) {
        return list("user.id = ?1 and accountType = ?2", userId, accountType);
    }

    public boolean accountNumberExists(String accountNumber) {
        return count("accountNumber", accountNumber) > 0;
    }

    //get balance
    public Double getBalance(Long accountNumber) {
        Account account = findById(accountNumber);
        return account.getBalance();
    }
}
