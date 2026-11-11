package com.monentreprise.rh.config;

import com.monentreprise.rh.entity.Role;
import com.monentreprise.rh.entity.User;
import com.monentreprise.rh.repository.RoleRepository;
import com.monentreprise.rh.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initialisation des données de référence au démarrage de l'application.
 * Crée les rôles et le compte admin par défaut si absents.
 *
 * Le mot de passe est lu depuis application.properties (app.security.admin-password)
 * — jamais codé en dur dans la classe.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER  = "ROLE_USER";

    private final RoleRepository    roleRepository;
    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;

    @Value("${app.security.admin-username}")
    private String adminUsername;

    @Value("${app.security.admin-email}")
    private String adminEmail;

    @Value("${app.security.admin-password}")
    private String adminPasswordClair; // lu depuis properties, hashé immédiatement

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository  = roleRepository;
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Créer les rôles s'ils n'existent pas
        Role roleAdmin = creerRoleSiAbsent(ROLE_ADMIN);
        Role roleUser  = creerRoleSiAbsent(ROLE_USER);

        // 2. Créer le compte admin par défaut s'il n'existe pas
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPasswordClair)); // hashage immédiat
            admin.setEnabled(true);
            admin.ajouterRole(roleAdmin);
            admin.ajouterRole(roleUser);
            userRepository.save(admin);
            log.warn(">>> Compte admin créé : '{}' — CHANGEZ le mot de passe en production !", adminUsername);
        }
    }

    private Role creerRoleSiAbsent(String nomRole) {
        return roleRepository.findByName(nomRole).orElseGet(() -> {
            log.info("Création du rôle : {}", nomRole);
            return roleRepository.save(new Role(nomRole));
        });
    }
}
