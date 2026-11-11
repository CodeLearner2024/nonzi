package com.monentreprise.rh.service.impl;

import com.monentreprise.rh.dto.RegisterForm;
import com.monentreprise.rh.entity.Role;
import com.monentreprise.rh.entity.User;
import com.monentreprise.rh.exception.EmailDejaExistantException;
import com.monentreprise.rh.exception.UsernameDejaExistantException;
import com.monentreprise.rh.repository.RoleRepository;
import com.monentreprise.rh.repository.UserRepository;
import com.monentreprise.rh.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service d'authentification / inscription.
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_USER = "ROLE_USER";

    private final UserRepository  userRepository;
    private final RoleRepository  roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.roleRepository  = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User inscrire(RegisterForm form) {
        // Vérifications d'unicité
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new UsernameDejaExistantException(form.getUsername());
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new EmailDejaExistantException(form.getEmail());
        }

        // Attribution du rôle par défaut
        Role roleUser = roleRepository.findByName(ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Rôle " + ROLE_USER + " introuvable en base."));

        User user = new User();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword())); // hashage BCrypt
        user.setEnabled(true);
        user.ajouterRole(roleUser);

        return userRepository.save(user);
    }
}
