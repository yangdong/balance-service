package io.evan.balance.common.command.validator;

import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;
import io.evan.balance.common.command.Required;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequiredValidatorTest {

    private RequiredValidator requiredValidator;

    @BeforeEach
    void setUp() {
        requiredValidator = new RequiredValidator();
    }

    @Test
    void testValidateWithNullObject() {
        Required required = new Required() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Required.class;
            }

            @Override
            public String errorCode() {
                return "NULL_ERROR";
            }
        };

        Result<Boolean, CommonErrorCode> result = requiredValidator.validate(required, null);
        assertFalse(result.isSuccess());
        assertEquals("NULL_ERROR", result.getError().code());
    }

    @Test
    void testValidateWithEmptyString() {
        Required required = new Required() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Required.class;
            }

            @Override
            public String errorCode() {
                return "EMPTY_STRING_ERROR";
            }
        };

        Result<Boolean, CommonErrorCode> result = requiredValidator.validate(required, "   ");
        assertFalse(result.isSuccess());
        assertEquals("EMPTY_STRING_ERROR", result.getError().code());
    }

    @Test
    void testValidateWithNonEmptyString() {
        Required required = new Required() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Required.class;
            }

            @Override
            public String errorCode() {
                return "NON_EMPTY_STRING_ERROR";
            }
        };

        Result<Boolean, CommonErrorCode> result = requiredValidator.validate(required, "valid");
        assertTrue(result.isSuccess());
    }

    @Test
    void testValidateWithEmptyMap() {
        Required required = new Required() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Required.class;
            }

            @Override
            public String errorCode() {
                return "EMPTY_MAP_ERROR";
            }
        };

        Result<Boolean, CommonErrorCode> result = requiredValidator.validate(required, Map.of());
        assertFalse(result.isSuccess());
        assertEquals("EMPTY_MAP_ERROR", result.getError().code());
    }

    @Test
    void testValidateWithNonEmptyMap() {
        Required required = new Required() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Required.class;
            }

            @Override
            public String errorCode() {
                return "NON_EMPTY_MAP_ERROR";
            }
        };

        Result<Boolean, CommonErrorCode> result = requiredValidator.validate(required, Map.of("key", "value"));
        assertTrue(result.isSuccess());
    }

    @Test
    void testValidateWithEmptyCollection() {
        Required required = new Required() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Required.class;
            }

            @Override
            public String errorCode() {
                return "EMPTY_COLLECTION_ERROR";
            }
        };

        Result<Boolean, CommonErrorCode> result = requiredValidator.validate(required, List.of());
        assertFalse(result.isSuccess());
        assertEquals("EMPTY_COLLECTION_ERROR", result.getError().code());
    }

    @Test
    void testValidateWithNonEmptyCollection() {
        Required required = new Required() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Required.class;
            }

            @Override
            public String errorCode() {
                return "NON_EMPTY_COLLECTION_ERROR";
            }
        };

        Result<Boolean, CommonErrorCode> result = requiredValidator.validate(required, List.of("item"));
        assertTrue(result.isSuccess());
    }
}