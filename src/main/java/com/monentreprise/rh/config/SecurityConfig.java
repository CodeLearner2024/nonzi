package com.monentreprise.rh.config;

import com.monentreprise.rh.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Configuration Spring Security 6 (sans WebSecurityConfigurerAdapter).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // active @PreAuthorize sur les méthodes de service
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // -------------------------------------------------------------------------
    // Encodeur de mot de passe BCrypt
    // -------------------------------------------------------------------------

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // -------------------------------------------------------------------------
    // Fournisseur d'authentification DAO
    // -------------------------------------------------------------------------

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        // Ne pas révéler si l'utilisateur n'existe pas vs mot de passe incorrect
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    // -------------------------------------------------------------------------
    // Chaîne de filtres de sécurité principale
    // -------------------------------------------------------------------------

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Autorisation des URLs
            .authorizeHttpRequests(auth -> auth
                // Ressources publiques (assets statiques + webjars)
                .requestMatchers("/login", "/register").permitAll()
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**",
                    "/webjars/**", "/favicon.ico"
                ).permitAll()
                // Actuator health (accessible sans auth pour le healthcheck Docker)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Console H2 (dev uniquement — à supprimer en production)
                .requestMatchers("/h2-console/**").permitAll()
                // Zone admin
                .requestMatchers("/users/**").hasRole("ADMIN")
                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            )

            // Formulaire de connexion personnalisé
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
            )

            // Déconnexion
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)         // invalide la session
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // Gestion de session : protection contre la fixation de session
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()  // nouveau session ID après login
                .maximumSessions(1)                  // une seule session par utilisateur
            )

            // Fournisseur d'authentification
            .authenticationProvider(authenticationProvider())

            // Désactiver la protection X-Frame-Options pour la console H2 (dev)
            .headers(headers -> headers.frameOptions(f -> f.sameOrigin()));

        return http.build();
    }

    // -------------------------------------------------------------------------
    // Nécessaire pour que maximumSessions fonctionne
    // -------------------------------------------------------------------------

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
