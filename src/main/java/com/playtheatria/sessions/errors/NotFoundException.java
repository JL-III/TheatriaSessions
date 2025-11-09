package com.playtheatria.sessions.errors;

public final class NotFoundException extends RepositoryException {
    public NotFoundException(String message) {
        super(message);
    }
}
