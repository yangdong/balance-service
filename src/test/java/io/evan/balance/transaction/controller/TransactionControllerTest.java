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

class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void createTransaction_with_invalid_amount() {
        NewTransferCommand command = new NewTransferCommand("sourceAccount", "targetAccount", "invalidAmount");

        ResponseEntity<APIResponse<TransferResponse>> response = transactionController.createTransaction(command);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("transfer.amount.invalid", response.getBody().getCode());
    }

    @Test
    void createTransaction_with_valid_amount() {
        NewTransferCommand command = new NewTransferCommand("sourceAccount", "targetAccount", "100.00");
        Transaction transaction = Transaction.builder().transactionId("transactionId").build();
        when(transactionService.transfer(any(TransferRequest.class)))
                .thenReturn(Result.success(transaction));

        ResponseEntity<APIResponse<TransferResponse>> response = transactionController.createTransaction(command);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("ok", response.getBody().getCode());
        assertEquals("transactionId", response.getBody().getData().getTransactionId());
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}