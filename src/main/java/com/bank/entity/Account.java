package com.bank.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

//database table for account details
@Entity
@Table(name = "account",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "accountType"}) //1 user can have a maximum of 3 accounts/ 1userid=3accountid
})
@SequenceGenerator(name = "account_seq", sequenceName = "account_SEQ", allocationSize = 1)

public class Account extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String accountNumber;

    @Column(nullable = false)
    public Double balance;

    //1.2.0
    @Column(nullable = false)
    public String accountType; //Debit, Credit, Savings

    //1.1.0
    @Column
    public Double creditLimit;

    @Column
    public Double interestRate; //annual interest rate (ex. 2.5%)

    @Column
    public LocalDateTime lastInterestCalculatedAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "fromAccount", fetch = FetchType.LAZY)
    public List<Transaction> sentTransactions;

    @JsonIgnore
    @OneToMany(mappedBy = "toAccount", fetch = FetchType.LAZY)
    public List<Transaction> receivedTransactions;

    public Account() {
        this.balance = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Account(String accountNumber, Double balance, String accountType, User user) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountType = accountType;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor for Savings account with interest rate
    public Account(String accountNumber, Double balance, String accountType, Double interestRate, User user) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountType = accountType;
        this.interestRate = interestRate;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastInterestCalculatedAt = LocalDateTime.now();
    }

    //getters and setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String n) { this.accountNumber = n; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; this.updatedAt = LocalDateTime.now(); }

    public String getAccountType() { return accountType; }
    public void setAccountType(String t) { this.accountType = t; }

    public Double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(Double limit) { this.creditLimit = limit; }

    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double rate) { this.interestRate = rate; }

    public LocalDateTime getLastInterestCalculatedAt() { return lastInterestCalculatedAt; }
    public void setLastInterestCalculatedAt(LocalDateTime date) { this.lastInterestCalculatedAt = date; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime u) { this.updatedAt = u; }

    public List<Transaction> getSentTransactions() { return sentTransactions; }
    public void setSentTransactions(List<Transaction> st) { this.sentTransactions = st; }

    public List<Transaction> getReceivedTransactions() { return receivedTransactions; }
    public void setReceivedTransactions(List<Transaction> rt) { this.receivedTransactions = rt; }

    public boolean isCredit() {
        return "CREDIT".equalsIgnoreCase(this.accountType);
    }
    public boolean isDebit() {
        return "DEBIT".equalsIgnoreCase(this.accountType);
    }
    public boolean isSavings() { return "SAVINGS".equalsIgnoreCase(this.accountType); }
}



