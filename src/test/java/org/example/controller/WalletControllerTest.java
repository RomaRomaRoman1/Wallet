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

@WebMvcTest(WalletController.class)
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService; // Используем @MockBean вместо @Mock

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        objectMapper = new ObjectMapper();
    }

    @Test
    public void testPerformOperation_Success() throws Exception {
        WalletOperationRequest request = new WalletOperationRequest(
                UUID.randomUUID(), WalletOperationRequest.OperationType.DEPOSIT, BigDecimal.valueOf(100)
        );

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Operation successful"));
    }

    @Test
    public void testPerformOperation_WalletNotFound() throws Exception {
        WalletOperationRequest request = new WalletOperationRequest(
                UUID.randomUUID(), WalletOperationRequest.OperationType.DEPOSIT, BigDecimal.valueOf(100)
        );

        doThrow(new WalletNotFoundException(request.getWalletId())).when(walletService).processOperation(any());

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Wallet not found with UUID: " + request.getWalletId()));
    }

    @Test
    public void testPerformOperation_InsufficientFunds() throws Exception {
        WalletOperationRequest request = new WalletOperationRequest(
                UUID.randomUUID(), WalletOperationRequest.OperationType.WITHDRAW, BigDecimal.valueOf(100)
        );

        doThrow(new InsufficientFundsException(request.getAmount())).when(walletService).processOperation(any());

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient funds for amount: " + request.getAmount()));
    }
}