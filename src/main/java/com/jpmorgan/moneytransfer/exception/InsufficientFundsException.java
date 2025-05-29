package com.jpmorgan.moneytransfer.exception;

public class InsufficientFundsException extends RuntimeException {
    
    public InsufficientFundsException(String message) {
        super(message);
    }
}