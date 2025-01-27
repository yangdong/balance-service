package io.evan.balance.common.command.validator;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;
import io.evan.balance.common.command.CommandReturns;
import io.evan.balance.common.command.Required;

public class RequiredValidator implements Validator {
    @Override
    public Result<Boolean, CommonErrorCode> validate(final Annotation annotation, final Object obj) {
        if (!(annotation instanceof Required required)) {
            return CommandReturns.SUCCESS;
        }

        if (obj == null
                || obj instanceof String && ((String) obj).trim().isEmpty()
                || (obj instanceof Map && ((Map) obj).isEmpty())
                || (obj instanceof Collection && ((Collection) obj).isEmpty())) {

            return Result.error(new CommonErrorCode(required.errorCode(), ""));
        }

        return CommandReturns.SUCCESS;
    }
}
