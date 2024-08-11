package org.example.repository;

import org.apache.catalina.User;
import org.example.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig // Аннотация для указания Spring конфигурации в тестах.
@DataJpaTest // Аннотация для тестирования JPA репозиториев, настраивает контекст для работы с JPA.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // Аннотация для автоматической конфигурации базы данных, позволяет использовать встроенную H2 базу данных для тестов.
public class WalletRepositoryTest { // Объявление класса WalletRepositoryTest.

    @Autowired // Аннотация для автоматического внедрения зависимости walletRepository.
    private WalletRepository walletRepository;// Поле для репозитория WalletRepository.
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OnlinePurchaseRepository onlinePurchaseRepository;
    @Autowired
    private OfflinePurchaseRepository offlinePurchaseRepository;

    static Client client = new Client();
    static Wallet wallet = new Wallet();
    static OfflinePurchase offlinePurchase = new OfflinePurchase();
    static OnlinePurchase onlinePurchase = new OnlinePurchase();
    @Autowired
    private PurchaseRepository purchaseRepository;

    @Test
    public void testCreateUser() {
        Client client = new Client(UUID.randomUUID(), "Roman", "Roman", "rom@mail.ru");
        userRepository.save(client);
        Client foundUser = userRepository.findById(client.getId()).orElse(null);
        assertEquals(client.getId(), foundUser.getId());
        assertEquals(client.getWallets(), client.getWallets());
        assertEquals(client.getEmail(), client.getEmail());
    }
    @Test // Аннотация для обозначения тестового метода.
    public void testSaveAndFindWallet() { // Метод для тестирования сохранения и поиска кошелька.
        Client client = new Client(UUID.randomUUID(), "Roman", "Roman", "rom@mail.ru");
        userRepository.save(client);
        Wallet wallet = new Wallet(UUID.randomUUID(), BigDecimal.valueOf(100), 1L, client); // Создание нового объекта Wallet с уникальным UUID, балансом 100 и версией 1.
        walletRepository.save(wallet); // Сохранение объекта Wallet в базе данных.

        Wallet foundWallet = walletRepository.findByIdWithLock(wallet.getId()).orElse(null); // Поиск сохраненного Wallet по его ID.
        assertEquals(wallet.getId(), foundWallet.getId()); // Проверка, что ID найденного кошелька совпадает с исходным ID.
        assertEquals(wallet.getBalance(), foundWallet.getBalance()); // Проверка, что баланс найденного кошелька совпадает с исходным балансом.
    }

    @Test // Аннотация для обозначения тестового метода.
    public void testChangeBalanceInWallet() { // Метод для тестирования изменения баланса в кошельке.
        Client client = new Client(UUID.randomUUID(), "Roman", "Roman", "rom@mail.ru");
        userRepository.save(client);
        Wallet wallet = new Wallet(UUID.randomUUID(), BigDecimal.valueOf(100), 1L, client); // Создание нового объекта Wallet с уникальным UUID, балансом 100 и версией 1.
        walletRepository.save(wallet); // Сохранение объекта Wallet в базе данных.

        Wallet foundWallet = walletRepository.findByIdWithLock(wallet.getId()).orElse(null); // Поиск сохраненного Wallet по его ID.
        foundWallet.setBalance(BigDecimal.valueOf(200)); // Изменение баланса найденного кошелька на 200.
        assertEquals(foundWallet.getBalance(), BigDecimal.valueOf(200)); // Проверка, что новый баланс найденного кошелька равен 200.
    }
    @Test
    public void testAddOnlinePurchase() {
        onlinePurchase = setOnlinePurchase();
        OnlinePurchase foundOnlinePurchase = onlinePurchaseRepository.findById(WalletRepositoryTest.onlinePurchase.getId()).orElse(null);
        assertNotNull(foundOnlinePurchase);
        assertEquals(foundOnlinePurchase.getId(), WalletRepositoryTest.onlinePurchase.getId());
        assertEquals(foundOnlinePurchase.getStoreUrl(), WalletRepositoryTest.onlinePurchase.getStoreUrl());
    }
    @Test
    public void testAddOfflinePurchase() {
        offlinePurchase = setOfflinePurchase();
        OfflinePurchase foundOfflinePurchase = offlinePurchaseRepository.findById(WalletRepositoryTest.offlinePurchase.getId()).orElse(null);
        assertNotNull(foundOfflinePurchase);
        assertEquals(foundOfflinePurchase.getId(), WalletRepositoryTest.offlinePurchase.getId());
        assertEquals(foundOfflinePurchase.getStoreAddress(), WalletRepositoryTest.offlinePurchase.getStoreAddress());
    }
    @Test
    public void tesAllPurchase() {
        List<Purchase> allPurchaseFromLocal = new ArrayList<>();
        List<Purchase> allPurchaseFromJpa = new ArrayList<>();
        offlinePurchase = setOfflinePurchase();
        onlinePurchase = setOnlinePurchase();
        allPurchaseFromLocal.addAll(Arrays.asList(offlinePurchase, onlinePurchase));
        allPurchaseFromJpa = purchaseRepository.findAll();
        assertEquals(allPurchaseFromLocal.size(), allPurchaseFromJpa.size());
        assertIterableEquals(allPurchaseFromLocal, allPurchaseFromJpa); // Проверка, что коллекции содержат одни и те же элементы
        assertEquals(allPurchaseFromLocal, allPurchaseFromJpa); // Проверка на полное равенство
        assertTrue(allPurchaseFromJpa.containsAll(allPurchaseFromLocal)); // Проверка, что все элементы из allPurchaseFromLocal содержатся в allPurchaseFromJpa
        assertTrue(allPurchaseFromLocal.containsAll(allPurchaseFromJpa)); // Проверка, что все элементы из allPurchaseFromJpa содержатся в allPurchaseFromLocal
    }
   public OfflinePurchase setOfflinePurchase() {
       Client client = new Client(UUID.randomUUID(), "Roman", "Roman", "rom@mail.ru");
       userRepository.save(client);
       Wallet wallet = new Wallet(UUID.randomUUID(), BigDecimal.valueOf(100), 1L, client);
       walletRepository.save(wallet);

       // Создаем онлайн-покупку и ассоциируем ее с кошельком
       OfflinePurchase offlinePurchase = new OfflinePurchase();
       offlinePurchase.setId(UUID.randomUUID());
       offlinePurchase.setStoreAddress("Moscow, read place, 117");
       offlinePurchase.setAmount(BigDecimal.valueOf(50));  // Устанавливаем сумму покупки
       offlinePurchase.setWallet(wallet);  // Ассоциируем покупку с кошельком
       offlinePurchaseRepository.save(offlinePurchase);
       return offlinePurchase;
   }
    public OnlinePurchase setOnlinePurchase() {
        Client client = new Client(UUID.randomUUID(), "Roman", "Roman", "rom@mail.ru");
        userRepository.save(client);
        Wallet wallet = new Wallet(UUID.randomUUID(), BigDecimal.valueOf(100), 1L, client);
        walletRepository.save(wallet);

        // Создаем онлайн-покупку и ассоциируем ее с кошельком
        OnlinePurchase onlinePurchase = new OnlinePurchase();
        onlinePurchase.setId(UUID.randomUUID());
        onlinePurchase.setStoreUrl("ozon.ru");
        onlinePurchase.setAmount(BigDecimal.valueOf(50));  // Устанавливаем сумму покупки
        onlinePurchase.setWallet(wallet);  // Ассоциируем покупку с кошельком
        onlinePurchaseRepository.save(onlinePurchase);
        return onlinePurchase;
    }

}