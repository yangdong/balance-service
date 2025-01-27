package io.evan.balance.common.command.validator;

import java.lang.annotation.Annotation;

import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;

public interface Validator {
    Result<Boolean, CommonErrorCode> validate(Annotation annotation, Object object);
}
