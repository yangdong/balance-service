package io.evan.balance.common.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;
import io.evan.balance.common.command.validator.ValidatorPool;

public class GeneralValidator {
    private final ResourceCommand<?> target;
    private final ValidatorPool validatorPool;

    public GeneralValidator(final ResourceCommand<?> target, final ValidatorPool validatorPool) {
        this.target = target;
        this.validatorPool = validatorPool;
    }

    public Result<Boolean, CommonErrorCode> validate() {
        if (target == null) {
            return Result.success(false);
        }

        final List<Field> fields = getAllFields(target.getClass());

        for (Field field : fields) {
            var checked = doFieldValidation(target, field);

            if (!checked.isSuccess()) {
                return checked;
            }
        }

        return CommandReturns.SUCCESS;
    }

    private static Object tryGetFieldValue(final Object object, final Field field) {
        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private Result<Boolean, CommonErrorCode> doFieldValidation(final ResourceCommand<?> target, final Field field) {
        Object value = tryGetFieldValue(target, field);

        for (var annotation : field.getAnnotations()) {
            var checked = this.validatorPool
                    .select(annotation)
                    .map(validator -> validator.validate(annotation, value))
                    .orElse(CommandReturns.SUCCESS);

            if (!checked.isSuccess()) {
                return checked;
            }
        }

        if (value != null) {
            Result<Boolean, CommonErrorCode> nestedResult = doNestedCommandValidate(value);
            if (!nestedResult.isSuccess()) {
                return nestedResult;
            }
        }

        return CommandReturns.SUCCESS;
    }

    private Result<Boolean, CommonErrorCode> doNestedCommandValidate(final Object value) {
        if (value instanceof ResourceCommand) {
            return ((ResourceCommand<?>) value).check();
        } else if (value instanceof Collection<?>) {
            for (Object o : (Collection<?>) value) {
                if (o instanceof ResourceCommand) {
                    var checked = ((ResourceCommand<?>) o).check();

                    if (!checked.isSuccess()) {
                        return checked;
                    }
                }
            }

            return CommandReturns.SUCCESS;
        } else if (value instanceof Map<?, ?>) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                Object v = entry.getValue();
                if (v instanceof ResourceCommand) {
                    var checked = ((ResourceCommand<?>) v).check();
                    if (!checked.isSuccess()) {
                        return checked;
                    }
                }
            }
        }

        return CommandReturns.SUCCESS;
    }

    private List<Field> getAllFields(final Class<?> clazz) {
        Class<?> localClazz = clazz;
        List<Field> fields = new ArrayList<>();
        while (localClazz != null && localClazz != Object.class) {
            fields.addAll(Arrays.asList(localClazz.getDeclaredFields()));
            localClazz = localClazz.getSuperclass();
        }
        return fields;
    }
}