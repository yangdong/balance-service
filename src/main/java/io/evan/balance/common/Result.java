package io.evan.balance.common;

public class Result<T, E extends ErrorCode> {
    private final T data;
    private final E error;

    public Result(final T data, final E error) {
        this.data = data;
        this.error = error;
    }

    public static <T, E extends ErrorCode> Result<T, E> success(T data) {
        return new Result<>(data, null);
    }

    public static <T, E extends ErrorCode> Result<T, E> error(E error) {
        return new Result<>(null, error);
    }

    public T getData() {
        return data;
    }

    public E getError() {
        return this.error;
    }

    public boolean hasError() {
        return this.error != null;
    }

    public boolean isSuccess() {
        return error == null;
    }
}