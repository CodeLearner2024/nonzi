package com.monentreprise.rh.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Formulaire de création/modification d'un utilisateur par un ADMIN.
 */
@Getter
@Setter
@NoArgsConstructor
public class UserForm {

    private Long id;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Entre 3 et 50 caractères")
    private String username;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 100)
    private String email;

    /**
     * Mot de passe : optionnel en modification (si vide → conserve l'existant).
     * Obligatoire en création.
     */
    @Size(min = 8, max = 100, message = "Au moins 8 caractères")
    private String password;

    private String confirmPassword;

    private boolean enabled = true;

    /** Ids des rôles sélectionnés dans le formulaire. */
    private Set<Long> roleIds = new HashSet<>();
}
