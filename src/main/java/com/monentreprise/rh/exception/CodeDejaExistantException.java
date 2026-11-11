package com.monentreprise.rh.exception;

/**
 * Exception métier levée quand un code TypePersonne est déjà utilisé.
 */
public class CodeDejaExistantException extends RuntimeException {

    public CodeDejaExistantException(String code) {
        super("Le code « " + code + " » est déjà utilisé par un autre type de personne.");
    }
}
