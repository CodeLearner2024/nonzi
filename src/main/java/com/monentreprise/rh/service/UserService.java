package com.monentreprise.rh.service;

import com.monentreprise.rh.dto.UserForm;
import com.monentreprise.rh.entity.Role;
import com.monentreprise.rh.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Contrat de service pour la gestion des utilisateurs (zone ADMIN).
 */
public interface UserService {

    Page<User> listerAvecRecherche(String motCle, Pageable pageable);

    User trouverParId(Long id);

    User creer(UserForm form);

    User modifier(Long id, UserForm form);

    void supprimer(Long id, String usernameConnecte);

    void reinitialiserMotDePasse(Long id, String nouveauMotDePasse, String usernameConnecte);

    List<Role> listerTousLesRoles();

    UserForm versFormulaire(User user);
}
