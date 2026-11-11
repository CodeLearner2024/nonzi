package com.monentreprise.rh.exception;

/**
 * Exception levée quand un TypePersonne est introuvable par son identifiant.
 */
public class TypePersonneIntrouvableException extends RuntimeException {

    public TypePersonneIntrouvableException(Long id) {
        super("Aucun type de personne trouvé avec l'identifiant : " + id);
    }
}
