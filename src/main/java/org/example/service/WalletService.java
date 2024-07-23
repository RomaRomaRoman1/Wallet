package org.example.service;

import org.springframework.transaction.annotation.Transactional;
import org.example.dto.WalletOperationRequest;
import org.example.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.entity.Wallet;
import org.example.exception.WalletNotFoundException;
import org.example.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.transaction.annotation.Isolation;

@Service  // Указывает, что этот класс является сервисом в бизнес-логике приложения.
public class WalletService {
    private final WalletRepository walletRepository;  // Репозиторий для работы с сущностями Wallet в базе данных.

    @Autowired  // Автоматическая инъекция зависимости. Spring автоматически внедрит реализацию WalletRepository в конструктор.
    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;  // Инициализация репозитория через конструктор.
    }

    private final Map<UUID, RateLimiter> walletRateLimiters = new ConcurrentHashMap<>();
    // Хранит RateLimiter для управления количеством запросов к кошелькам. Используется потокобезопасная коллекция.
    // Нужен в первую очередь для того, чтобы запоминать уже существующие ограничения на обращения к каждому кошельку.

    @Transactional(isolation = Isolation.SERIALIZABLE)  // Обозначает, что метод должен выполняться в рамках одной транзакции. Все операции в методе будут зафиксированы как единое целое.
    public void processOperation(WalletOperationRequest request) throws WalletNotFoundException, InsufficientFundsException {
        RateLimiter rateLimiter = walletRateLimiters.computeIfAbsent(request.getWalletId(), k -> RateLimiter.create(1000));
        // Получение или создание RateLimiter для указанного идентификатора кошелька. RateLimiter используется для ограничения
        // запросов в секунду для обращения к каждому кошельку учитывая запрос баланса
        rateLimiter.acquire();  // Ограничение на 1000 запросов в секунду

        // Поиск кошелька по идентификатору из запроса. Если кошелек не найден, выбрасывается исключение WalletNotFoundException.
        // Используем пессимистичную блокировку для предотвращения одновременного изменения данных
        Wallet wallet = walletRepository.findByIdWithLock(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException(request.getWalletId()));

        // Проверка типа операции. Если операция - снятие средств (WITHDRAW), выполняем соответствующую логику.
        if (request.getOperationType() == WalletOperationRequest.OperationType.WITHDRAW) {
            // Проверка, достаточно ли средств на кошельке для выполнения операции. Если нет, выбрасывается исключение InsufficientFundsException.
            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException(request.getAmount());
            }
            // Уменьшение баланса кошелька на указанную сумму.
            wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        } else {
            // В случае, если операция - пополнение (DEPOSIT), увеличиваем баланс кошелька на указанную сумму.
            wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        }

        // Сохранение обновленного кошелька в базе данных.
        walletRepository.save(wallet);
    }

    // Метод для получения баланса кошелька по его идентификатору.
    public BigDecimal getBalance(UUID walletId) throws WalletNotFoundException {
        // Получение или создание RateLimiter для указанного идентификатора кошелька. RateLimiter используется для ограничения
        // запросов в секунду для обращения к каждому кошельку учитывая запрос баланса
        RateLimiter rateLimiter = walletRateLimiters.computeIfAbsent(walletId, k -> RateLimiter.create(1000));

        rateLimiter.acquire();  // Ограничение на 1000 запросов в секунду
        // Поиск кошелька по идентификатору. Если кошелек не найден, выбрасывается исключение WalletNotFoundException.
        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        // Возврат текущего баланса кошелька.
        return wallet.getBalance();
    }
}
