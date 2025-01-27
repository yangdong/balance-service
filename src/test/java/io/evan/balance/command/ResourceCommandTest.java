package io.evan.balance.command;

import org.junit.jupiter.api.Test;

import io.evan.balance.common.CommonErrorCode;
import io.evan.balance.common.Result;
import io.evan.balance.common.command.CommandReturns;
import io.evan.balance.common.command.Required;
import io.evan.balance.common.command.ResourceCommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Account {
    private String name;

    public Account(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

class NewAccountCommand extends ResourceCommand<Account> {
    @Required(errorCode = "new.account.name.missing")
    private final String name;

    public NewAccountCommand(final String name) {
        this.name = name;
    }

    @Override
    public Result<Boolean, CommonErrorCode> doCheck() {
        if (this.name == null || this.name.length() > 20) {
            return Result.error(new CommonErrorCode(
                    "new.account.name.invalid",
                    "Account name must be less than 20 characters")
            );
        }

        return CommandReturns.SUCCESS;
    }

    @Override
    public Account toResource() {
        return new Account(this.name);
    }
}

public class ResourceCommandTest {
    @Test
    void test_customized_validation() {
        NewAccountCommand command = new NewAccountCommand("this_is_a_name_beyond_20_characters");
        Result<Account, CommonErrorCode> result = command.resource();
        assertFalse(result.isSuccess());
        assertEquals("new.account.name.invalid", result.getError().code());
    }

    @Test
    void test_required_but_missing() {
        NewAccountCommand command = new NewAccountCommand(null);
        Result<Account, CommonErrorCode> result = command.resource();
        assertFalse(result.isSuccess());
        assertEquals("new.account.name.missing", result.getError().code());
    }

    @Test
    void test_valid_command() {
        NewAccountCommand command = new NewAccountCommand("new_name");
        Result<Account, CommonErrorCode> result = command.resource();
        assertTrue(result.isSuccess());
        assertEquals(result.getData().getName(), "new_name");
    }
}