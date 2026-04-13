package com.bank.repository;

import com.bank.entity.Account;
import com.bank.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;


//handles the database queries related to accounts
@ApplicationScoped
public class AccountRepository implements PanacheRepository<Account> {

    public Optional<Account> findByAccountNumber(String accountNumber) {
        return find("accountNumber", accountNumber).firstResultOptional();
    }

    public List<Account> findByUserId(Long userId) {
        return list("user.id", userId);
    }

    public boolean accountNumberExists(String accountNumber) {
        return count("accountNumber", accountNumber) > 0;
    }

    public boolean userHasAccountType(Long userId, String accountType) {
        return find("user.id = ?1 AND accountType = ?2", userId, accountType).firstResultOptional().isPresent();
    }
}
