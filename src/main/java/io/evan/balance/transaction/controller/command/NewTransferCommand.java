package io.evan.balance.transaction.controller.command;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;
import io.evan.balance.common.command.Required;
import io.evan.balance.common.command.ResourceCommand;
import io.evan.balance.common.command.CommandReturns;
import io.evan.balance.transaction.service.TransferRequest;

public class NewTransferCommand extends ResourceCommand<TransferRequest> {
    @Required(errorCode = "transfer.source.account.missing")
    private final String sourceAccount;
    @Required(errorCode = "transfer.target.account.missing")
    private final String targetAccount;
    @Required(errorCode = "transfer.amount.missing")
    private final String amount;

    public NewTransferCommand(@JsonProperty("sourceAccount") final String sourceAccount,
                              @JsonProperty("targetAccount") String targetAccount,
                              @JsonProperty("amount") String amount) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
    }

    @Override
    public Result<Boolean, CommonErrorCode> doCheck() {
        try {
            new BigDecimal(amount);
        } catch (NumberFormatException e) {
            return Result.error(new CommonErrorCode("transfer.amount.invalid", "Transfer amount must be number"));
        }
        return CommandReturns.SUCCESS;
    }

    @Override
    protected TransferRequest toResource() {
        return new TransferRequest(sourceAccount, targetAccount, new BigDecimal(amount));
    }
}
