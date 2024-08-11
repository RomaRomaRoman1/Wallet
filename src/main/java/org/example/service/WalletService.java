package org.example.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.apache.catalina.User;
import org.example.dto.ClientDto;
import org.example.entity.Client;
import org.example.entity.OfflinePurchase;
import org.example.entity.OnlinePurchase;
import org.example.exception.AlreadyExistWIthThisEmail;
import org.example.repository.OfflinePurchaseRepository;
import org.example.repository.OnlinePurchaseRepository;
import org.example.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.example.dto.WalletOperationRequest;
import org.example.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.entity.Wallet;
import org.example.exception.WalletNotFoundException;
import org.example.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.transaction.annotation.Isolation;

@Service  // Указывает, что этот класс является сервисом в бизнес-логике приложения.
public class WalletService {
    private final WalletRepository walletRepository;  // Репозиторий для работы с сущностями Wallet в базе данных.
    private final UserRepository userRepository;
    private final OnlinePurchaseRepository onlinePurchaseRepository;
    private final OfflinePurchaseRepository offlinePurchaseRepository;

    @Autowired
    // Автоматическая инъекция зависимости. Spring автоматически внедрит реализацию WalletRepository в конструктор.
    public WalletService(WalletRepository walletRepository, UserRepository userRepository, OnlinePurchaseRepository onlinePurchaseRepository, OfflinePurchaseRepository offlinePurchaseRepository) {
        this.walletRepository = walletRepository;  // Инициализация репозитория через конструктор.
        this.userRepository = userRepository;
        this.onlinePurchaseRepository = onlinePurchaseRepository;
        this.offlinePurchaseRepository = offlinePurchaseRepository;
    }

    private final Map<UUID, RateLimiter> walletRateLimiters = new ConcurrentHashMap<>();
    // Хранит RateLimiter для управления количеством запросов к кошелькам. Используется потокобезопасная коллекция.
    // Нужен в первую очередь для того, чтобы запоминать уже существующие ограничения на обращения к каждому кошельку.
    RateLimiterConfig config = RateLimiterConfig.custom()
            .limitForPeriod(10) // Максимальное количество вызовов за период
            .limitRefreshPeriod(Duration.ofMinutes(1)) // Период обновления лимита
            .timeoutDuration(Duration.ofSeconds(2)) // Время ожидания при превышении лимита
            .build();

    @Transactional
    public Client createUser(ClientDto userDto) throws AlreadyExistWIthThisEmail {
        Optional<User> existingUser = userRepository.findByEmail(userDto.getEmail());
            if (existingUser.isPresent()) {
                throw new AlreadyExistWIthThisEmail(userDto.getEmail());
        }
         return userRepository.save(new Client(userDto.getId(), userDto.getUsername(), userDto.getUserSurname(), userDto.getEmail()));
    }
    @Transactional
    public OnlinePurchase addOnlinePurchase(String storeAddress, BigDecimal amount, UUID walletId) throws WalletNotFoundException, InsufficientFundsException {
        // Найти кошелек по ID
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(amount);
        }
        // Создать объект онлайн-покупки
        OnlinePurchase onlinePurchase = new OnlinePurchase();
        onlinePurchase.setId(UUID.randomUUID()); // Генерируем новый UUID для покупки
        onlinePurchase.setAmount(amount);
        onlinePurchase.setWallet(wallet);
        onlinePurchase.setStoreUrl(storeAddress);
        // Обновить баланс кошелька
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        // Сохранить покупку
        return (onlinePurchaseRepository.save(onlinePurchase));
    }
    @Transactional
    public OfflinePurchase addOfflinePurchase(String storAddress, BigDecimal amount, UUID walletId) throws WalletNotFoundException, InsufficientFundsException {
        // Найти кошелек по ID
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(amount);
        }
        // Создать объект онлайн-покупки
        OfflinePurchase offlinePurchase = new OfflinePurchase();
        offlinePurchase.setId(UUID.randomUUID()); // Генерируем новый UUID для покупки
        offlinePurchase.setAmount(amount);
        offlinePurchase.setWallet(wallet);
        offlinePurchase.setStoreAddress(storAddress);
        // Обновить баланс кошелька
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        // Сохранить покупку
        return (offlinePurchaseRepository.save(offlinePurchase));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void processOperation(WalletOperationRequest request) throws WalletNotFoundException, InsufficientFundsException {
        RateLimiter rateLimiter = walletRateLimiters.computeIfAbsent(request.getWalletId(), k ->
                RateLimiter.of("myRateLimiter", config));

        try {
            Runnable operation = RateLimiter.decorateRunnable(rateLimiter, () -> {
                try {
                    Wallet wallet = walletRepository.findByIdWithLock(request.getWalletId())
                            .orElseThrow(() -> new WalletNotFoundException(request.getWalletId()));

                    if (request.getOperationType() == WalletOperationRequest.OperationType.WITHDRAW) {
                        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                            throw new InsufficientFundsException(request.getAmount());
                        }
                        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
                    } else {
                        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
                    }

                    walletRepository.save(wallet);
                } catch (WalletNotFoundException | InsufficientFundsException e) {
                    throw new RuntimeException(e);
                }
            });

            operation.run();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof WalletNotFoundException) {
                throw (WalletNotFoundException) e.getCause();
            } else if (e.getCause() instanceof InsufficientFundsException) {
                throw (InsufficientFundsException) e.getCause();
            }
            throw e;
        }
    }

    public BigDecimal getBalance(UUID walletId) throws WalletNotFoundException {
        RateLimiter rateLimiter = walletRateLimiters.computeIfAbsent(walletId, k ->
                RateLimiter.of("myRateLimiter", config));

        final BigDecimal[] balance = new BigDecimal[1];

        try {
            Runnable operation = RateLimiter.decorateRunnable(rateLimiter, () -> {
                try {
                    Wallet wallet = walletRepository.findByIdWithLock(walletId)
                            .orElseThrow(() -> new WalletNotFoundException(walletId));
                    balance[0] = wallet.getBalance();
                } catch (WalletNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });

            operation.run();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof WalletNotFoundException) {
                throw (WalletNotFoundException) e.getCause();
            }
            throw e;
        }

        return balance[0];
    }
    }
