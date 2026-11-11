package com.monentreprise.rh.exception;

public class UserIntrouvableException extends RuntimeException {
    public UserIntrouvableException(Long id) {
        super("Aucun utilisateur trouvé avec l'identifiant : " + id);
    }
}
