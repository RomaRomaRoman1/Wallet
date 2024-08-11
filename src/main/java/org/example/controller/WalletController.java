package org.example.controller;

import org.aspectj.weaver.ast.Var;
import org.example.dto.ClientDto;
import org.example.dto.WalletOperationRequest;
import org.example.entity.Client;
import org.example.exception.AlreadyExistWIthThisEmail;
import org.example.exception.InsufficientFundsException;
import org.example.exception.WalletNotFoundException;
import org.example.repository.OnlinePurchaseRepository;
import org.example.repository.UserRepository;
import org.example.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

import java.math.BigDecimal;


@RestController // Аннотация, которая указывает, что этот класс является REST контроллером и возвращает данные непосредственно в HTTP-ответе.
@RequestMapping("/api/v1/wallet") // Устанавливает базовый URL для всех методов в этом контроллере.
public class WalletController {

    private final WalletService walletService; // Сервис для обработки операций с кошельками.
    private final UserRepository userRepository;
    private final OnlinePurchaseRepository onlinePurchaseRepository;

    @Autowired // Аннотация, указывающая Spring автоматически внедрит зависимость через конструктор.
    public WalletController(WalletService walletService, UserRepository userRepository, OnlinePurchaseRepository onlinePurchaseRepository) {
        this.walletService = walletService; // Инициализация сервиса через конструктор.
        this.userRepository = userRepository;
        this.onlinePurchaseRepository = onlinePurchaseRepository;
    }

    @PostMapping // Обрабатывает HTTP POST запросы по пути "/api/v1/wallet".
    //@RequestBody указывает Spring, что тело запроса должно быть десериализовано в объект класса WalletOperationRequest
    public ResponseEntity<String> performOperation(@RequestBody WalletOperationRequest request) throws WalletNotFoundException, InsufficientFundsException {
            // Вызов метода обработки операции из сервиса. Если возникнут ошибки, они будут пойманы в блоке catch.
            walletService.processOperation(request);
            // Если операция прошла успешно, возвращаем HTTP статус 200 (OK) с сообщением об успешном выполнении.
            return ResponseEntity.ok("Operation successful");

    }

    @GetMapping("/{walletId}")
    public ResponseEntity<String> getBalance(@PathVariable UUID walletId) throws WalletNotFoundException {

            BigDecimal balance = walletService.getBalance(walletId);
            return ResponseEntity.ok(String.valueOf(balance));
        }
        @PostMapping("/{client}")
    public ResponseEntity<String> createClient(@RequestBody ClientDto clientDto) throws AlreadyExistWIthThisEmail {
        walletService.createUser(clientDto);
        return ResponseEntity.ok("Client has been created");
    }
    @PostMapping("/{online}")
    public ResponseEntity<String> addOnlinePurchase (@RequestParam String storeAddress,@RequestParam BigDecimal amount,@RequestParam UUID walletId) throws WalletNotFoundException, InsufficientFundsException {
        walletService.addOnlinePurchase(storeAddress, amount, walletId);
        return ResponseEntity.ok("Purchase has been saved");
    }
    @PostMapping("/{online}")
    public ResponseEntity<String> addOfflinePurchase (@RequestParam String storeUrl,@RequestParam BigDecimal amount,@RequestParam UUID walletId) throws WalletNotFoundException, InsufficientFundsException {
        walletService.addOnlinePurchase(storeUrl, amount, walletId);
        return ResponseEntity.ok("Purchase has been saved");
    }
    }