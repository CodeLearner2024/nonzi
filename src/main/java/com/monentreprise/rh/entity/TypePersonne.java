package com.monentreprise.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entité JPA représentant un type de personne dans l'organisation.
 * Exemples : EMP/Employé, STG/Stagiaire, CTR/Contractuel.
 */
@Entity
@Table(name = "type_personne")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TypePersonne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code court unique (ex: EMP, STG).
     * Sans espaces, max 20 caractères.
     */
    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20, message = "Le code ne peut pas dépasser 20 caractères")
    @Pattern(regexp = "\\S+", message = "Le code ne doit pas contenir d'espaces")
    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;

    /**
     * Désignation lisible (ex: Employé, Stagiaire).
     */
    @NotBlank(message = "La désignation est obligatoire")
    @Size(max = 100, message = "La désignation ne peut pas dépasser 100 caractères")
    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    public TypePersonne(String code, String designation) {
        this.code = code;
        this.designation = designation;
    }
}
