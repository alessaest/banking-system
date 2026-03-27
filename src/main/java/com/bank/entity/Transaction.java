package com.bank.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

//database table for transaction details
@Entity
@Table(name = "transaction")
@SequenceGenerator(name = "transaction_seq", sequenceName = "transaction_SEQ", allocationSize = 1)
public class Transaction extends PanacheEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    public Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    public Account toAccount;

    @Column(nullable = false)
    public Long userId;

    @Column(nullable = false)
    public Double amount;

    @Column(nullable = false)
    public String type; //transfer, deposit, withdrawal

    @Column(nullable = false)
    public String status; //completed, pending, failed

    public LocalDateTime dateTime;
    public String description;

    public Transaction() {
        this.dateTime = LocalDateTime.now();
        this.status = "Completed";
    }

    public Transaction(Account fromAccount, Account toAccount, Long userId, Double amount, String type, String status, String description) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.status = status != null ? status : "Completed";
        this.dateTime = LocalDateTime.now();
        this.description = description != null ? description : "";
    }

    //getters and setters
    public Account getFromAccount() { return fromAccount; }
    public void setFromAccount(Account a) { this.fromAccount = a; }

    public Account getToAccount() { return toAccount; }
    public void setToAccount(Account a) { this.toAccount = a; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double a) { this.amount = a; }

    public String getType() { return type; }
    public void setType(String t) { this.type = t; }

    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dt) { this.dateTime = dt; }

    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
}
