package io.evan.balance.account.controller;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import io.evan.balance.account.AccountErrorCode;
import io.evan.balance.account.service.AccountInfo;
import io.evan.balance.account.service.AccountService;
import io.evan.balance.common.APIResponse;
import io.evan.balance.common.IdGenerator;
import io.evan.balance.common.Result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private AccountController accountController;

    @Test
    void create_freeze_failure() {
        String accountId = "123456";
        BigDecimal amount = BigDecimal.valueOf(100);
        Result<AccountInfo, AccountErrorCode> errorResult = Result.error(AccountErrorCode.ACCOUNT_NOT_FOUND);

        when(idGenerator.generate()).thenReturn("generatedId");
        when(accountService.freeze(eq("generatedId"), eq(accountId), eq(amount), eq("freeze"))).thenReturn(errorResult);

        ResponseEntity<APIResponse<AccountInfo>> response = accountController.createFreeze(accountId, amount);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(AccountErrorCode.ACCOUNT_NOT_FOUND.code(), response.getBody().getCode());
    }

    @Test
    void create_freeze_success() {
        String accountId = "123456";
        BigDecimal amount = BigDecimal.valueOf(100);
        AccountInfo accountInfo = new AccountInfo();
        Result<AccountInfo, AccountErrorCode> successResult = Result.success(accountInfo);

        when(idGenerator.generate()).thenReturn("generatedId");
        when(accountService.freeze(eq("generatedId"), eq(accountId), eq(amount), eq("freeze"))).thenReturn(successResult);

        ResponseEntity<APIResponse<AccountInfo>> response = accountController.createFreeze(accountId, amount);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("ok", response.getBody().getCode());
        assertEquals(accountInfo, response.getBody().getData());
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}