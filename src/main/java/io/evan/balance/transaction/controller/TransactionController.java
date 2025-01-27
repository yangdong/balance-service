package io.evan.balance.transaction.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.evan.balance.common.APIResponse;
import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;
import io.evan.balance.transaction.controller.command.NewTransferCommand;
import io.evan.balance.transaction.domain.Transaction;
import io.evan.balance.transaction.error.TransactionErrorCode;
import io.evan.balance.transaction.service.TransactionService;
import io.evan.balance.transaction.service.TransferRequest;
import io.evan.balance.transaction.service.TransferResponse;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping()
    public ResponseEntity<APIResponse<TransferResponse>> createTransaction(@RequestBody final NewTransferCommand command) {
        final Result<TransferRequest, CommonErrorCode> resource = command.resource();

        if (resource.hasError()) {
            return ResponseEntity.badRequest().body(
                    APIResponse.error(resource.getError())
            );
        }

        final Result<Transaction, TransactionErrorCode> transferResult = this.transactionService.transfer(resource.getData());

        if (transferResult.hasError()) {
            return ResponseEntity.badRequest().body(
                    APIResponse.error(transferResult.getError())
            );
        }

        return ResponseEntity.ok(APIResponse.success(new TransferResponse(transferResult.getData().getTransactionId(), true)));
    }
}