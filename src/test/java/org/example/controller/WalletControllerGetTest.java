package org.example.controller;
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

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(WalletController.class)
public class WalletControllerGetTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetBalance_Success() throws Exception {
        UUID walletId = UUID.randomUUID();
        String balance = String.valueOf(BigDecimal.valueOf(100));

        // Устанавливаем поведение mock-сервиса: при вызове getBalance с указанным walletId возвращать BigDecimal.valueOf(100)
        when(walletService.getBalance(walletId)).thenReturn(BigDecimal.valueOf(100));

        // Выполняем GET запрос к API с указанным walletId
        mockMvc.perform(get("/api/v1/wallet/{walletId}", walletId)
                        .contentType(MediaType.APPLICATION_JSON))
                // Ожидаем, что статус ответа будет 200 OK
                .andExpect(status().isOk())
                // Ожидаем, что тело ответа будет строкой, представляющей баланс
                .andExpect(content().string(balance));
    }

    @Test
    public void testGetBalance_WalletNotFound() throws Exception {
        UUID walletId = UUID.randomUUID();

        // Устанавливаем поведение mock-сервиса: при вызове getBalance с указанным walletId выбрасывать WalletNotFoundException
        doThrow(new WalletNotFoundException(walletId)).when(walletService).getBalance(walletId);

        // Выполняем GET запрос к API с указанным walletId
        mockMvc.perform(get("/api/v1/wallet/{walletId}", walletId)
                        .accept(MediaType.APPLICATION_JSON))
                // Ожидаем, что статус ответа будет 400 Bad Request
                .andExpect(status().isBadRequest())
                // Ожидаем, что тело ответа будет строкой, представляющей сообщение об ошибке
                .andExpect(content().string("Wallet not found with UUID: " + walletId));
    }
}
