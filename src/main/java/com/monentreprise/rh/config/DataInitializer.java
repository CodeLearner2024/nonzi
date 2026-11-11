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
 * Initialisation des données de référence au démarrage.
 * Crée les rôles ROLE_ADMIN / ROLE_USER et le compte admin par défaut
 * si aucun admin n'existe encore en base.
 *
 * Les valeurs sont lues depuis des variables d'environnement :
 *   ADMIN_USERNAME, ADMIN_EMAIL, ADMIN_PASSWORD
 * (configurées dans application.yml via ${ADMIN_*}).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER  = "ROLE_USER";

    private final RoleRepository  roleRepository;
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Nom d'utilisateur admin — lu depuis ${ADMIN_USERNAME} */
    @Value("${app.security.admin-username}")
    private String adminUsername;

    /** Email admin — lu depuis ${ADMIN_EMAIL} */
    @Value("${app.security.admin-email}")
    private String adminEmail;

    /**
     * Mot de passe admin en clair — lu depuis ${ADMIN_PASSWORD}.
     * Hashé immédiatement par BCrypt, jamais stocké ni loggé en clair.
     */
    @Value("${app.security.admin-password}")
    private String adminPasswordClair;

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
        // Création des rôles s'ils n'existent pas
        Role roleAdmin = creerRoleSiAbsent(ROLE_ADMIN);
        Role roleUser  = creerRoleSiAbsent(ROLE_USER);

        // Création du compte admin uniquement si aucun admin n'existe
        boolean adminExiste = userRepository.findByUsername(adminUsername)
                .map(u -> u.aLeRole(ROLE_ADMIN))
                .orElse(false);

        if (!adminExiste) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPasswordClair)); // BCrypt — jamais en clair
            admin.setEnabled(true);
            admin.ajouterRole(roleAdmin);
            admin.ajouterRole(roleUser);
            userRepository.save(admin);
            log.warn(">>> Compte admin créé : '{}' — Changez le mot de passe en production !", adminUsername);
        }
    }

    private Role creerRoleSiAbsent(String nomRole) {
        return roleRepository.findByName(nomRole).orElseGet(() -> {
            log.info("Création du rôle : {}", nomRole);
            return roleRepository.save(new Role(nomRole));
        });
    }
}
