package com.playtheatria.theatriaSessions.result;

public record Ok<T, E extends Throwable>(T value) implements Result<T, E> {}
