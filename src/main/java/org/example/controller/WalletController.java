package org.example.controller;

import org.example.dto.WalletOperationRequest;
import org.example.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.exception.WalletNotFoundException;
import org.example.exception.InsufficientFundsException;
import java.util.UUID;

import java.math.BigDecimal;


@RestController // Аннотация, которая указывает, что этот класс является REST контроллером и возвращает данные непосредственно в HTTP-ответе.
@RequestMapping("/api/v1/wallet") // Устанавливает базовый URL для всех методов в этом контроллере.
public class WalletController {

    private final WalletService walletService; // Сервис для обработки операций с кошельками.

    @Autowired // Аннотация, указывающая Spring автоматически внедрит зависимость через конструктор.
    public WalletController(WalletService walletService) {
        this.walletService = walletService; // Инициализация сервиса через конструктор.
    }

    @PostMapping // Обрабатывает HTTP POST запросы по пути "/api/v1/wallet".
    //@RequestBody указывает Spring, что тело запроса должно быть десериализовано в объект класса WalletOperationRequest
    public ResponseEntity<String> performOperation(@RequestBody WalletOperationRequest request) {
        try {
            // Вызов метода обработки операции из сервиса. Если возникнут ошибки, они будут пойманы в блоке catch.
            walletService.processOperation(request);
            // Если операция прошла успешно, возвращаем HTTP статус 200 (OK) с сообщением об успешном выполнении.
            return ResponseEntity.ok("Operation successful");
        } catch (WalletNotFoundException | InsufficientFundsException e) {
            // Если произошла ошибка, возвращаем HTTP статус 400 (Bad Request) и сообщение об ошибке.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<String> getBalance(@PathVariable UUID walletId) {
        try {
            BigDecimal balance = walletService.getBalance(walletId);
            return ResponseEntity.ok(String.valueOf(balance));
        } catch (WalletNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}