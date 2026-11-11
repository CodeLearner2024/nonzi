package com.monentreprise.rh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO / objet de formulaire pour la création et la modification d'un TypePersonne.
 * Découplé de l'entité pour permettre l'évolution indépendante des deux.
 */
@Getter
@Setter
@NoArgsConstructor
public class TypePersonneForm {

    /** Id non nul uniquement en mode modification. */
    private Long id;

    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20, message = "Le code ne peut pas dépasser 20 caractères")
    @Pattern(regexp = "\\S+", message = "Le code ne doit pas contenir d'espaces")
    private String code;

    @NotBlank(message = "La désignation est obligatoire")
    @Size(max = 100, message = "La désignation ne peut pas dépasser 100 caractères")
    private String designation;

    public TypePersonneForm(Long id, String code, String designation) {
        this.id = id;
        this.code = code;
        this.designation = designation;
    }
}
