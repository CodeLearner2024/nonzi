package com.monentreprise.rh.exception;

public class UsernameDejaExistantException extends RuntimeException {
    public UsernameDejaExistantException(String username) {
        super("Le nom d'utilisateur « " + username + " » est déjà utilisé.");
    }
}
