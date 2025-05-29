package com.jpmorgan.moneytransfer.repository.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    private String accountNumber;

    @NotNull
    @Column(nullable = false)
    private String ownerName;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @NotNull
    @Column(nullable = false, length = 3)
    private String currencyCode;

    // Version for optimistic locking
    @Version
    private Long version;

    public Account() {
    }

    public Account(String accountNumber, String ownerName, BigDecimal balance, String currencyCode) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance;
        this.currencyCode = currencyCode;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrencyCode() { return currencyCode; }

    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}