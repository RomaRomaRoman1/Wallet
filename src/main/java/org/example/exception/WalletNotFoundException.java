package org.example.exception;

import java.util.UUID;

public class WalletNotFoundException extends Exception {
    public WalletNotFoundException(UUID walletId) {
        super("Wallet not found with UUID: " + walletId.toString());
    }
}