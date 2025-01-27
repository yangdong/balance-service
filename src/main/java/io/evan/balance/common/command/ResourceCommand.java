package io.evan.balance.common.command;

import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;
import io.evan.balance.common.command.validator.ValidatorPool;

public abstract class ResourceCommand<T> {
    public Result<T, CommonErrorCode> resource() {
        final Result<Boolean, CommonErrorCode> checked = check();

        if (!checked.isSuccess()) {
            return Result.error(checked.getError());
        }

        return Result.success(toResource());
    }

    protected abstract T toResource();

    public Result<Boolean, CommonErrorCode> check() {
        final Result<Boolean, CommonErrorCode> checked =
                new GeneralValidator(this, ValidatorPool.INSTANCE).validate();

        return checked.isSuccess() ? doCheck() : checked;
    }

    public Result<Boolean, CommonErrorCode> doCheck() {
        return Result.success(true);
    }
}