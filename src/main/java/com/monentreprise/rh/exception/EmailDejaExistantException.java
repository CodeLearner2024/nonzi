package com.monentreprise.rh.exception;

public class EmailDejaExistantException extends RuntimeException {
    public EmailDejaExistantException(String email) {
        super("L'adresse email « " + email + " » est déjà utilisée.");
    }
}
