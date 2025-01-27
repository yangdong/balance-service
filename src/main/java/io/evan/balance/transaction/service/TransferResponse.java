package io.evan.balance.transaction.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class TransferResponse {
    @JsonProperty("transactionId")
    private final String transactionId;
    @JsonProperty("success")
    private final boolean success;

    public TransferResponse(final String transactionId, final boolean success) {
        this.transactionId = transactionId;
        this.success = success;
    }
}
