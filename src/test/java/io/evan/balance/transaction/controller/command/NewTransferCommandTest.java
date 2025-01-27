package io.evan.balance.transaction.controller.command;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;
import io.evan.balance.transaction.service.TransferRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewTransferCommandTest {

    @Test
    void do_check_with_invalid_amount() {
        NewTransferCommand command = new NewTransferCommand("sourceAccount", "targetAccount", "invalidAmount");
        Result<Boolean, CommonErrorCode> result = command.doCheck();
        assertTrue(result.hasError());
        assertEquals("transfer.amount.invalid", result.getError().code());
        assertEquals("Transfer amount must be number", result.getError().message());
    }

    @Test
    void do_check_with_valid_amount() {
        NewTransferCommand command = new NewTransferCommand("sourceAccount", "targetAccount", "100.00");
        Result<Boolean, CommonErrorCode> result = command.doCheck();
        assertTrue(result.isSuccess());
    }

    @Test
    void to_resource_conversion() {
        NewTransferCommand command = new NewTransferCommand("sourceAccount", "targetAccount", "100.00");
        TransferRequest request = command.toResource();
        assertEquals("sourceAccount", request.getSourceAccount());
        assertEquals("targetAccount", request.getTargetAccount());
        assertEquals(new BigDecimal("100.00"), request.getAmount());
    }
}