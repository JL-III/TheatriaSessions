package com.playtheatria.theatriaSessions.errors;

public final class PersistenceException extends RepositoryException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
