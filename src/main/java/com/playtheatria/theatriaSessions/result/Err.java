package com.playtheatria.theatriaSessions.result;

public record Err<T, E extends Throwable>(E error) implements Result<T, E> {
    public T value() throws E {
        throw error;
    }
}
