package com.playtheatria.theatriaSessions.result;

public sealed interface Result<T, E extends Throwable> permits Ok, Err {
    T value() throws E;
}