package io.evan.balance.common.command.validator;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;

import io.evan.balance.common.command.Required;

public class ValidatorPool {
    public static final ValidatorPool INSTANCE = new ValidatorPool();
    private static final Map<Class<? extends Annotation>, Validator> validatorPool =
            Map.ofEntries(
                    Map.entry(Required.class, new RequiredValidator())
            );

    public Optional<Validator> select(final Annotation annotation) {
        return Optional.ofNullable(validatorPool.get(annotation.annotationType()));
    }
}