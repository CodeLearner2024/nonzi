package com.monentreprise.rh.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rôle de sécurité associé à un utilisateur.
 * Valeurs : ROLE_ADMIN, ROLE_USER.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom du rôle au format Spring Security : ROLE_XXX */
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    public Role(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
