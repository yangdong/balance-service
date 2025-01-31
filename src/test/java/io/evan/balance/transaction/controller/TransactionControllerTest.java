package io.evan.balance.transaction.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import io.evan.balance.common.APIResponse;
import io.evan.balance.common.Result;
import io.evan.balance.transaction.controller.command.NewTransferCommand;
import io.evan.balance.transaction.domain.Transaction;
import io.evan.balance.transaction.service.TransactionService;
import io.evan.balance.transaction.service.TransferRequest;
import io.evan.balance.transaction.service.TransferResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import io.evan.balance.common.APIResponse;
import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;
import io.evan.balance.transaction.controller.command.NewTransferCommand;
import io.evan.balance.transaction.domain.Transaction;
import io.evan.balance.transaction.error.TransactionException;
import io.evan.balance.transaction.service.TransactionService;
import io.evan.balance.transaction.service.TransferRequest;
import io.evan.balance.transaction.service.TransferResponse;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTransactionSuccess() throws Exception {
        NewTransferCommand command = new NewTransferCommand("acc_000001", "acc_000002", "100");
        Transaction transaction = Transaction.builder()
                .transactionId("txn_123")
                .sourceAccountNumber("acc_000001")
                .targetAccountNumber("acc_000002")
                .build();

        when(transactionService.transfer(any(TransferRequest.class))).thenReturn(transaction);

        // Act
        ResponseEntity<APIResponse<TransferResponse>> response = transactionController.createTransaction(command);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("txn_123", response.getBody().getData().getTransactionId());
    }

    @Test
    void testCreateTransactionInvalidAmount() {
        NewTransferCommand command = new NewTransferCommand("acc_000001", "acc_000002", "invalid_amount");

        // Act
        ResponseEntity<APIResponse<TransferResponse>> response = transactionController.createTransaction(command);

        // Assert
        assertEquals(400, response.getStatusCode().value());
    }
}