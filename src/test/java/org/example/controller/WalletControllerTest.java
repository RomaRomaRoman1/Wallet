package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.WalletOperationRequest;
import org.example.exception.InsufficientFundsException;
import org.example.exception.WalletNotFoundException;
import org.example.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(WalletController.class) // Аннотация для тестирования только слоя Web MVC (контроллеров) в Spring Boot
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc; // Объект для имитации HTTP-запросов к контроллерам

    @MockBean
    private WalletService walletService; // Мок-объект для WalletService, используем @MockBean для интеграции с Spring

    private ObjectMapper objectMapper; // Объект для преобразования Java объектов в JSON и обратно

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // Инициализация мок-объектов перед каждым тестом

        objectMapper = new ObjectMapper(); // Инициализация ObjectMapper для преобразования JSON
    }

    @Test
    public void testPerformOperation_Success() throws Exception {
        // Создание объекта WalletOperationRequest с случайным UUID, типом операции DEPOSIT и суммой 100
        WalletOperationRequest request = new WalletOperationRequest(
                UUID.randomUUID(), WalletOperationRequest.OperationType.DEPOSIT, BigDecimal.valueOf(100)
        );

        // Выполнение HTTP POST запроса к "/api/v1/wallet"
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON) // Установка типа контента запроса как JSON
                        .content(objectMapper.writeValueAsString(request))) // Преобразование объекта request в JSON-строку
                .andExpect(status().isOk()) // Ожидание HTTP статуса 200 (OK)
                .andExpect(content().string("Operation successful")); // Ожидание текста ответа "Operation successful"
    }

    @Test
    public void testPerformOperation_WalletNotFound() throws Exception {
        // Создание объекта WalletOperationRequest с случайным UUID, типом операции DEPOSIT и суммой 100
        WalletOperationRequest request = new WalletOperationRequest(
                UUID.randomUUID(), WalletOperationRequest.OperationType.DEPOSIT, BigDecimal.valueOf(100)
        );

        // Настройка мока для броска исключения WalletNotFoundException при вызове processOperation
        doThrow(new WalletNotFoundException(request.getWalletId())).when(walletService).processOperation(any());

        // Выполнение HTTP POST запроса к "/api/v1/wallet"
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON) // Установка типа контента запроса как JSON
                        .content(objectMapper.writeValueAsString(request))) // Преобразование объекта request в JSON-строку
                .andExpect(status().isBadRequest()) // Ожидание HTTP статуса 400 (Bad Request)
                .andExpect(content().string("Wallet not found with UUID: " + request.getWalletId())); // Ожидание текста ошибки
    }

    @Test
    public void testPerformOperation_InsufficientFunds() throws Exception {
        // Создание объекта WalletOperationRequest с случайным UUID, типом операции WITHDRAW и суммой 100
        WalletOperationRequest request = new WalletOperationRequest(
                UUID.randomUUID(), WalletOperationRequest.OperationType.WITHDRAW, BigDecimal.valueOf(100)
        );

        // Настройка мока для броска исключения InsufficientFundsException при вызове processOperation
        doThrow(new InsufficientFundsException(request.getAmount())).when(walletService).processOperation(any());

        // Выполнение HTTP POST запроса к "/api/v1/wallet"
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON) // Установка типа контента запроса как JSON
                        .content(objectMapper.writeValueAsString(request))) // Преобразование объекта request в JSON-строку
                .andExpect(status().isBadRequest()) // Ожидание HTTP статуса 400 (Bad Request)
                .andExpect(content().string("Insufficient funds for amount: " + request.getAmount())); // Ожидание текста ошибки
    }
}