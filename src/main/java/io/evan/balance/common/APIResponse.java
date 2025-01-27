package io.evan.balance.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class APIResponse<T> {
    @JsonProperty("code")
    private final String code;
    @JsonProperty("message")
    private final String message;
    @JsonProperty("data")
    private final T data;

    private APIResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> APIResponse<T> error(ErrorCode errorCode) {
        return new APIResponse<>(errorCode.code(), errorCode.message(), null);
    }

    public static <T> APIResponse<T> fromResult(final Result<T, ? extends ErrorCode> result) {
        if (result == null) {
            return new APIResponse<>("ok", null, null);
        }

        if (result.hasError()) {
            return new APIResponse<>(result.getError().code(), result.getError().message(), null);
        }

        return new APIResponse<>("ok", null, result.getData());
    }

    public static <T> APIResponse<T> success(T data) {
        return new APIResponse<>("ok", null, data);
    }
}
