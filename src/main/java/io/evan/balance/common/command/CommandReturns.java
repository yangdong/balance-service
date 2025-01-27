package io.evan.balance.common.command;

import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;

public class CommandReturns {
    public static final Result<Boolean, CommonErrorCode> SUCCESS = Result.success(true);
}
