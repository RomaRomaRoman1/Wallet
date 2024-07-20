package org.example.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(BigDecimal amount) {
        super("Insufficient funds for amount: " + amount.toString());
    }
}
