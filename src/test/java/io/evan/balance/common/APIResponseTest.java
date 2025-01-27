package io.evan.balance.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class APIResponseTest {
    @Test
    void test_for_error_response() {
        ErrorCode errorCode = new ErrorCode() {
            @Override
            public String code() {
                return "error.code";
            }

            @Override
            public String message() {
                return "Error message";
            }
        };

        APIResponse<Object> response = APIResponse.error(errorCode);

        assertEquals("error.code", response.getCode());
        assertEquals("Error message", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void test_for_from_result_with_error() {
        ErrorCode errorCode = new ErrorCode() {
            @Override
            public String code() {
                return "error.code";
            }

            @Override
            public String message() {
                return "Error message";
            }
        };

        Result<Object, ErrorCode> result = Result.error(errorCode);
        APIResponse<Object> response = APIResponse.fromResult(result);

        assertEquals("error.code", response.getCode());
        assertEquals("Error message", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void test_for_from_result_with_null() {
        APIResponse<Object> response = APIResponse.fromResult(null);

        assertEquals("ok", response.getCode());
        assertNull(response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void test_for_from_result_with_success() {
        Object data = new Object();
        Result<Object, ErrorCode> result = Result.success(data);
        APIResponse<Object> response = APIResponse.fromResult(result);

        assertEquals("ok", response.getCode());
        assertNull(response.getMessage());
        assertEquals(data, response.getData());
    }

    @Test
    void test_for_success_response() {
        Object data = new Object();
        APIResponse<Object> response = APIResponse.success(data);

        assertEquals("ok", response.getCode());
        assertNull(response.getMessage());
        assertEquals(data, response.getData());
    }
}