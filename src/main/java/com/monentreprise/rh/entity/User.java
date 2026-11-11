package com.monentreprise.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Compte utilisateur de l'application RH.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    /** Mot de passe haché BCrypt — jamais en clair. */
    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    /** Compte actif (true) ou désactivé (false). */
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /** Rôles accordés à cet utilisateur. */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles",
        joinColumns        = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // -------------------------------------------------------------------------
    // Utilitaires
    // -------------------------------------------------------------------------

    public void ajouterRole(Role role) {
        this.roles.add(role);
    }

    public boolean aLeRole(String nomRole) {
        return roles.stream().anyMatch(r -> r.getName().equals(nomRole));
    }
}
