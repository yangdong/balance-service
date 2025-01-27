package io.evan.balance.account.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.evan.balance.account.AccountErrorCode;
import io.evan.balance.account.service.AccountInfo;
import io.evan.balance.account.service.AccountService;
import io.evan.balance.common.APIResponse;
import io.evan.balance.common.IdGenerator;
import io.evan.balance.common.Result;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {
    private final AccountService accountService;
    private final IdGenerator idGenerator;

    public AccountController(final AccountService accountService, final IdGenerator idGenerator) {
        this.accountService = accountService;
        this.idGenerator = idGenerator;
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<APIResponse<AccountInfo>> createFreeze(@PathVariable String accountId) {
        final Result<AccountInfo, AccountErrorCode> getAccountResult = accountService.getAccount(accountId);

        if (getAccountResult.isSuccess()) {
            return ResponseEntity.ok(APIResponse.success(getAccountResult.getData()));
        }

        return ResponseEntity.badRequest().body(APIResponse.error(getAccountResult.getError()));
    }

    @PostMapping("/{accountId}/freezes")
    public ResponseEntity<APIResponse<AccountInfo>> createFreeze(@PathVariable String accountId,
                                                                 @RequestParam(value = "amount") BigDecimal amount) {
        final Result<AccountInfo, AccountErrorCode> freezeResult = accountService.freeze(idGenerator.generate(), accountId, amount, "freeze");

        if (freezeResult.isSuccess()) {
            return ResponseEntity.ok(APIResponse.success(freezeResult.getData()));
        }

        return ResponseEntity.badRequest().body(APIResponse.error(freezeResult.getError()));
    }
}