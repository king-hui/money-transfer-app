package com.jpmorgan.moneytransfer.service;

import com.jpmorgan.moneytransfer.exception.InsufficientFundsException;
import com.jpmorgan.moneytransfer.repository.model.Account;
import com.jpmorgan.moneytransfer.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final CurrencyRateService currencyRateService;
    private final BigDecimal TRANSACTION_FEE = new BigDecimal("0.01");

    @Autowired
    public AccountService(AccountRepository accountRepository, CurrencyRateService currencyRateService) {
        this.accountRepository = accountRepository;
        this.currencyRateService = currencyRateService;
    }

    @Transactional(readOnly = true)
    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
    }

    @Transactional
    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    @Retryable(
            value = {CannotAcquireLockException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    @Transactional
    public void transferMoney(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount, String currencyCode) {
        // Validate input
        logger.info("Starting money transfer: {} {} from account {} to account {}",
                amount, currencyCode, sourceAccountNumber, destinationAccountNumber);

                validateInput(sourceAccountNumber, destinationAccountNumber, amount);
        System.out.println("Transfering " + currencyCode + amount + " from " + sourceAccountNumber + " to " + destinationAccountNumber);

        // Acquire locks in a consistent order
        Account sourceAccount = accountRepository.findByAccountNumberWithLock(sourceAccountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + sourceAccountNumber));

        Account destinationAccount = accountRepository.findByAccountNumberWithLock(destinationAccountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + destinationAccountNumber));

        // Perform the transfer
        withdraw(sourceAccount, amount, currencyCode);
        deposit(destinationAccount, amount, currencyCode);
    }

    private void validateInput(String sourceAccountNumber, String destinationAccountNumber, BigDecimal amount)
    {
        if (sourceAccountNumber.equals(destinationAccountNumber)) {
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
    }

    public void withdraw(Account account, final BigDecimal withdrawAmount, final String currencyCode)
    {
        BigDecimal amount = withdrawAmount;
        if (!account.getCurrencyCode().equals(currencyCode))
        {
            amount = currencyRateService.computeExchangeRate(currencyCode, account.getCurrencyCode(), amount);
        }
        BigDecimal transactionFee = computeTransactionFee(amount);
        amount = amount.add(transactionFee);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account: " + account.getAccountNumber());
        }

        account.setBalance(account.getBalance().subtract(amount));
        logger.info("Withdraw money: {} {} from account {}",
                amount, account.getCurrencyCode(), account.getOwnerName());
        accountRepository.save(account);
    }

    public void deposit(Account account, BigDecimal depositAmount, String currencyCode)
    {
        BigDecimal amount = depositAmount;
        if (!account.getCurrencyCode().equals(currencyCode))
        {
            amount = currencyRateService.computeExchangeRate(currencyCode, account.getCurrencyCode(), depositAmount);
        }

        account.setBalance(account.getBalance().add(amount));
        logger.info("Deposit money: {} {} to account {}",
                amount, account.getCurrencyCode(), account.getOwnerName());
        accountRepository.save(account);
    }

    private BigDecimal computeTransactionFee(BigDecimal amount) {
        return amount.multiply(TRANSACTION_FEE).setScale(4, RoundingMode.HALF_UP);
    }
}