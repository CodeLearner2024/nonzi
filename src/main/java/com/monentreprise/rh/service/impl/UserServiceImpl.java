package com.monentreprise.rh.service.impl;

import com.monentreprise.rh.dto.UserForm;
import com.monentreprise.rh.entity.Role;
import com.monentreprise.rh.entity.User;
import com.monentreprise.rh.exception.EmailDejaExistantException;
import com.monentreprise.rh.exception.UserIntrouvableException;
import com.monentreprise.rh.exception.UsernameDejaExistantException;
import com.monentreprise.rh.repository.RoleRepository;
import com.monentreprise.rh.repository.UserRepository;
import com.monentreprise.rh.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des utilisateurs.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final RoleRepository  roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.roleRepository  = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // -------------------------------------------------------------------------
    // Lecture
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<User> listerAvecRecherche(String motCle, Pageable pageable) {
        if (StringUtils.hasText(motCle)) {
            return userRepository.rechercherParMotCle(motCle.trim(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User trouverParId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserIntrouvableException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> listerTousLesRoles() {
        return roleRepository.findAll();
    }

    // -------------------------------------------------------------------------
    // Écriture
    // -------------------------------------------------------------------------

    @Override
    public User creer(UserForm form) {
        validerUnicite(form.getUsername(), form.getEmail(), null);

        Set<Role> roles = resoudreRoles(form.getRoleIds());

        User user = new User();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setEnabled(form.isEnabled());
        user.setRoles(roles);

        return userRepository.save(user);
    }

    @Override
    public User modifier(Long id, UserForm form) {
        User existant = trouverParId(id);
        validerUnicite(form.getUsername(), form.getEmail(), id);

        existant.setUsername(form.getUsername());
        existant.setEmail(form.getEmail());
        existant.setEnabled(form.isEnabled());
        existant.setRoles(resoudreRoles(form.getRoleIds()));

        // Mot de passe : on ne change que s'il est renseigné
        if (StringUtils.hasText(form.getPassword())) {
            existant.setPassword(passwordEncoder.encode(form.getPassword()));
        }

        return userRepository.save(existant);
    }

    @Override
    public void supprimer(Long id, String usernameConnecte) {
        User user = trouverParId(id);
        // Un admin ne peut pas se supprimer lui-même
        if (user.getUsername().equals(usernameConnecte)) {
            throw new IllegalStateException("Vous ne pouvez pas supprimer votre propre compte.");
        }
        userRepository.delete(user);
    }

    @Override
    public void reinitialiserMotDePasse(Long id, String nouveauMotDePasse, String usernameConnecte) {
        if (!StringUtils.hasText(nouveauMotDePasse) || nouveauMotDePasse.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
        }
        User user = trouverParId(id);
        user.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        userRepository.save(user);
    }

    // -------------------------------------------------------------------------
    // Utilitaires
    // -------------------------------------------------------------------------

    @Override
    public UserForm versFormulaire(User user) {
        UserForm form = new UserForm();
        form.setId(user.getId());
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        form.setEnabled(user.isEnabled());
        form.setRoleIds(user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet()));
        return form;
    }

    /** Vérifie l'unicité du username et de l'email (exclut l'id courant en modification). */
    private void validerUnicite(String username, String email, Long idExclu) {
        boolean usernamePris = (idExclu == null)
                ? userRepository.existsByUsername(username)
                : userRepository.existsByUsernameAndIdNot(username, idExclu);

        boolean emailPris = (idExclu == null)
                ? userRepository.existsByEmail(email)
                : userRepository.existsByEmailAndIdNot(email, idExclu);

        if (usernamePris) throw new UsernameDejaExistantException(username);
        if (emailPris)    throw new EmailDejaExistantException(email);
    }

    /** Charge les entités Role depuis la liste d'ids. */
    private Set<Role> resoudreRoles(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(roleRepository.findAllById(roleIds));
    }
}
