package io.evan.balance.account.service;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AccountInfo {
    @JsonProperty("accountNumber")
    private String accountNumber;
    @JsonProperty("balance")
    private BigDecimal balance;

    public AccountInfo(final String accountNumber, final BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
}
