package com.bank.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class Transaction extends PanacheEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    public Account fromAccount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    public Account toAccount;
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

    public Transaction(Account fromAccount, Account toAccount, Double amount, String type, String status, String description) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.type = type;
        this.status = status != null ? status : "Completed";
        this.dateTime = LocalDateTime.now();
        this.description = description != null ? description : "";
    }

    //getters and setters
    public Account getFromAccount() { return fromAccount; }
    public void setFromAccount(Account fromAccount) { this.fromAccount = fromAccount; }

    public Account getToAccount() { return toAccount; }
    public void setToAccount(Account toAccount) { this.toAccount = toAccount; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
