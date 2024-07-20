package org.example.repository;

import org.example.entity.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig // Аннотация для указания Spring конфигурации в тестах.
@DataJpaTest // Аннотация для тестирования JPA репозиториев, настраивает контекст для работы с JPA.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // Аннотация для автоматической конфигурации базы данных, позволяет использовать встроенную H2 базу данных для тестов.
public class WalletRepositoryTest { // Объявление класса WalletRepositoryTest.

    @Autowired // Аннотация для автоматического внедрения зависимости walletRepository.
    private WalletRepository walletRepository; // Поле для репозитория WalletRepository.

    @Test // Аннотация для обозначения тестового метода.
    public void testSaveAndFindWallet() { // Метод для тестирования сохранения и поиска кошелька.
        Wallet wallet = new Wallet(UUID.randomUUID(), BigDecimal.valueOf(100), 1L); // Создание нового объекта Wallet с уникальным UUID, балансом 100 и версией 1.
        walletRepository.save(wallet); // Сохранение объекта Wallet в базе данных.

        Wallet foundWallet = walletRepository.findById(wallet.getId()).orElse(null); // Поиск сохраненного Wallet по его ID.
        assertEquals(wallet.getId(), foundWallet.getId()); // Проверка, что ID найденного кошелька совпадает с исходным ID.
        assertEquals(wallet.getBalance(), foundWallet.getBalance()); // Проверка, что баланс найденного кошелька совпадает с исходным балансом.
    }

    @Test // Аннотация для обозначения тестового метода.
    public void testChangeBalanceInWallet() { // Метод для тестирования изменения баланса в кошельке.
        Wallet wallet = new Wallet(UUID.randomUUID(), BigDecimal.valueOf(100), 1L); // Создание нового объекта Wallet с уникальным UUID, балансом 100 и версией 1.
        walletRepository.save(wallet); // Сохранение объекта Wallet в базе данных.

        Wallet foundWallet = walletRepository.findById(wallet.getId()).orElse(null); // Поиск сохраненного Wallet по его ID.
        foundWallet.setBalance(BigDecimal.valueOf(200)); // Изменение баланса найденного кошелька на 200.
        assertEquals(foundWallet.getBalance(), BigDecimal.valueOf(200)); // Проверка, что новый баланс найденного кошелька равен 200.
    }
}