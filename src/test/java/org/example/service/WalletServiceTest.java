package org.example.service;

import org.example.dto.WalletOperationRequest;
import org.example.entity.Wallet;
import org.example.exception.InsufficientFundsException;
import org.example.exception.WalletNotFoundException;
import org.example.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository; // Создаем мок объект для WalletRepository

    @InjectMocks
    private WalletService walletService; // Внедряем моки в WalletService

    private UUID walletId;
    private Wallet wallet;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // Инициализируем моки перед каждым тестом
        walletId = UUID.randomUUID(); // Создаем уникальный UUID для кошелька
        wallet = new Wallet(walletId, BigDecimal.valueOf(100), 1L); // Создаем новый объект Wallet с начальным балансом
    }

    @Test
    public void testProcessOperation_Deposit() throws WalletNotFoundException, InsufficientFundsException {
        WalletOperationRequest request = new WalletOperationRequest(walletId, WalletOperationRequest.OperationType.DEPOSIT, BigDecimal.valueOf(50)); // Создаем запрос на депозит

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet)); // Настраиваем мок, чтобы вернуть созданный кошелек

        walletService.processOperation(request); // Выполняем операцию депозита

        assertEquals(BigDecimal.valueOf(150), wallet.getBalance()); // Проверяем, что баланс увеличился на 50
        verify(walletRepository, times(1)).save(wallet); // Проверяем, что метод save был вызван один раз
    }

    @Test
    public void testProcessOperation_Withdraw() throws WalletNotFoundException, InsufficientFundsException {
        WalletOperationRequest request = new WalletOperationRequest(walletId, WalletOperationRequest.OperationType.WITHDRAW, BigDecimal.valueOf(50)); // Создаем запрос на снятие

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet)); // Настраиваем мок, чтобы вернуть созданный кошелек

        walletService.processOperation(request); // Выполняем операцию снятия

        assertEquals(BigDecimal.valueOf(50), wallet.getBalance()); // Проверяем, что баланс уменьшился на 50
        verify(walletRepository, times(1)).save(wallet); // Проверяем, что метод save был вызван один раз
    }

    @Test
    public void testProcessOperation_InsufficientFunds() {
        WalletOperationRequest request = new WalletOperationRequest(walletId, WalletOperationRequest.OperationType.WITHDRAW, BigDecimal.valueOf(150)); // Создаем запрос на снятие больше текущего баланса

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet)); // Настраиваем мок, чтобы вернуть созданный кошелек

        assertThrows(InsufficientFundsException.class, () -> {
            walletService.processOperation(request); // Проверяем, что выбрасывается исключение InsufficientFundsException
        });

        verify(walletRepository, never()).save(any(Wallet.class)); // Проверяем, что метод save не был вызван
    }

    @Test
    public void testProcessOperation_WalletNotFound() {
        WalletOperationRequest request = new WalletOperationRequest(walletId, WalletOperationRequest.OperationType.DEPOSIT, BigDecimal.valueOf(50)); // Создаем запрос на депозит

        when(walletRepository.findById(walletId)).thenReturn(Optional.empty()); // Настраиваем мок, чтобы не возвращать кошелек

        assertThrows(WalletNotFoundException.class, () -> {
            walletService.processOperation(request); // Проверяем, что выбрасывается исключение WalletNotFoundException
        });

        verify(walletRepository, never()).save(any(Wallet.class)); // Проверяем, что метод save не был вызван
    }
}