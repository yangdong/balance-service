package io.evan.balance.common.command;

import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;
import io.evan.balance.common.command.validator.ValidatorPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class GeneralValidatorTest {

    @Mock
    private ResourceCommand<?> mockCommand;

    @Mock
    private ValidatorPool mockValidatorPool;

    private GeneralValidator generalValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generalValidator = new GeneralValidator(mockCommand, mockValidatorPool);
    }

    @Test
    void testValidateWithNullTarget() {
        GeneralValidator validator = new GeneralValidator(null, mockValidatorPool);
        Result<Boolean, CommonErrorCode> result = validator.validate();
        assertTrue(result.isSuccess());
        assertFalse(result.getData());
    }

    @Test
    void testValidateWithValidCommand() {
        when(mockCommand.check()).thenReturn(Result.success(true));
        Result<Boolean, CommonErrorCode> result = generalValidator.validate();
        assertTrue(result.isSuccess());
    }

    @Test
    void testValidateWithInvalidCommand() {
        when(mockCommand.check()).thenReturn(Result.error(null));
        Result<Boolean, CommonErrorCode> result = generalValidator.validate();
        assertFalse(result.hasError());
    }
}