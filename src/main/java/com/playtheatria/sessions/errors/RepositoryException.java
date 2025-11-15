package com.playtheatria.sessions.errors;

public sealed class RepositoryException extends Exception
        permits PersistenceException, NotFoundException {
    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
