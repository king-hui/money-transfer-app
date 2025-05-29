package com.jpmorgan.moneytransfer.controller;

import com.jpmorgan.moneytransfer.dto.TransferMoneyDto;
import com.jpmorgan.moneytransfer.exception.InsufficientFundsException;
import com.jpmorgan.moneytransfer.repository.model.Account;
import com.jpmorgan.moneytransfer.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.findByAccountNumber(accountNumber));
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody @Valid Account account) {
        return new ResponseEntity<>(accountService.createAccount(account), HttpStatus.CREATED);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(@RequestBody @Valid TransferMoneyDto transferRequest) {
        try {
            accountService.transferMoney(
                    transferRequest.getSourceAccountNumber(),
                    transferRequest.getDestinationAccountNumber(),
                    transferRequest.getAmount(),
                    transferRequest.getCurrencyCode()
            );
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Transfer completed successfully");
            
            return ResponseEntity.ok(response);
        } catch (InsufficientFundsException e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Transfer failed: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}