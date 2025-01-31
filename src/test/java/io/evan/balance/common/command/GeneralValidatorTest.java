package io.evan.balance.common.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;
import io.evan.balance.common.command.validator.ValidatorPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    public void testNestedCommand() {
        final Result<Boolean, CommonErrorCode> checked = new RootCommand(
                "test",
                new SubCommand(false),
                Arrays.asList(),
                Map.of()
        ).check();

        assertFalse(checked.isSuccess());
        assertEquals(checked.getError().code(), "test.error");
    }

    @Test
    public void testListNestedCommand() {
        final Result<Boolean, CommonErrorCode> checked = new RootCommand(
                "test",
                new SubCommand(true),
                Arrays.asList(new SubCommand(false)),
                Map.of()
        ).check();

        assertFalse(checked.isSuccess());
        assertEquals(checked.getError().code(), "test.error");
    }

    @Test
    public void testMapNestedCommand() {
        final Result<Boolean, CommonErrorCode> checked = new RootCommand(
                "test",
                null,
                null,
                Map.of("Success", new SubCommand(false))
        ).check();

        assertFalse(checked.isSuccess());
        assertEquals(checked.getError().code(), "test.error");
    }

    class RootCommand extends ResourceCommand<String> {
        private final String name;
        private final SubCommand subCommand;
        private final List<SubCommand> subCommands;
        private final Map<String, SubCommand> subCommandMap;

        public RootCommand(final String name,
                           final SubCommand subCommand,
                           final List<SubCommand> subCommands,
                           final Map<String, SubCommand> subCommandMap) {
            super();
            this.name = name;
            this.subCommand = subCommand;
            this.subCommands = subCommands;
            this.subCommandMap = subCommandMap;
        }

        @Override
        protected String toResource() {
            return "";
        }
    }

    class SubCommand extends ResourceCommand<Boolean> {
        private final boolean success;

        public SubCommand(final boolean success) {
            super();
            this.success = success;
        }

        @Override
        protected Boolean toResource() {
            return this.success;
        }

        @Override
        public Result<Boolean, CommonErrorCode> doCheck() {
            if (this.success) {
                return Result.success(this.success);
            }

            return Result.error(new CommonErrorCode("test.error", "Test error"));
        }
    }
}