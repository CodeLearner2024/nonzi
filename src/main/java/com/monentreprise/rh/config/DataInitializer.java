package com.monentreprise.rh.config;

import com.monentreprise.rh.entity.Role;
import com.monentreprise.rh.entity.User;
import com.monentreprise.rh.repository.RoleRepository;
import com.monentreprise.rh.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initialisation au démarrage :
 *   1. Crée les rôles ROLE_ADMIN et ROLE_USER s'ils n'existent pas.
 *   2. Crée le compte admin (admin / admin1234) à chaque démarrage
 *      s'il n'existe pas encore en base.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // Rôles fixes de l'application
    static final String ROLE_ADMIN = "ROLE_ADMIN";
    static final String ROLE_USER  = "ROLE_USER";

    // Compte admin par défaut — en dur, pas de variable d'environnement
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL    = "admin@rh.local";
    private static final String ADMIN_PASSWORD = "admin1234"; // hashé par BCrypt au démarrage

    private final RoleRepository  roleRepository;
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

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

        // 2. Créer le compte admin s'il n'existe pas encore
        if (!userRepository.existsByUsername(ADMIN_USERNAME)) {
            User admin = new User();
            admin.setUsername(ADMIN_USERNAME);
            admin.setEmail(ADMIN_EMAIL);
            admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD)); // BCrypt
            admin.setEnabled(true);
            admin.ajouterRole(roleAdmin);
            admin.ajouterRole(roleUser);
            userRepository.save(admin);
            log.info("Compte admin créé : username='{}', email='{}'", ADMIN_USERNAME, ADMIN_EMAIL);
        }
    }

    private Role creerRoleSiAbsent(String nomRole) {
        return roleRepository.findByName(nomRole).orElseGet(() -> {
            log.info("Création du rôle : {}", nomRole);
            return roleRepository.save(new Role(nomRole));
        });
    }
}
