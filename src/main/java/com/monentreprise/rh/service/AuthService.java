package com.monentreprise.rh.service;

import com.monentreprise.rh.dto.RegisterForm;
import com.monentreprise.rh.entity.User;

/**
 * Contrat de service pour l'authentification et l'inscription.
 */
public interface AuthService {

    /**
     * Inscrit un nouvel utilisateur avec le rôle ROLE_USER.
     *
     * @throws com.monentreprise.rh.exception.UsernameDejaExistantException si username pris
     * @throws com.monentreprise.rh.exception.EmailDejaExistantException    si email pris
     */
    User inscrire(RegisterForm form);
}
