package com.monentreprise.rh.service;

import com.monentreprise.rh.dto.TypePersonneForm;
import com.monentreprise.rh.entity.TypePersonne;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Contrat de service pour la gestion des types de personne.
 */
public interface TypePersonneService {

    /**
     * Retourne une page de types de personne filtrée par un mot-clé optionnel.
     *
     * @param motCle   filtre sur code et designation (peut être vide)
     * @param pageable paramètres de pagination et de tri
     * @return page de résultats
     */
    Page<TypePersonne> listerAvecRecherche(String motCle, Pageable pageable);

    /**
     * Retourne un type de personne par son identifiant.
     *
     * @throws com.monentreprise.rh.exception.TypePersonneIntrouvableException si absent
     */
    TypePersonne trouverParId(Long id);

    /**
     * Crée un nouveau type de personne à partir du formulaire.
     *
     * @throws com.monentreprise.rh.exception.CodeDejaExistantException si le code existe déjà
     */
    TypePersonne creer(TypePersonneForm form);

    /**
     * Met à jour un type de personne existant.
     *
     * @throws com.monentreprise.rh.exception.TypePersonneIntrouvableException si absent
     * @throws com.monentreprise.rh.exception.CodeDejaExistantException        si le code est pris
     */
    TypePersonne modifier(Long id, TypePersonneForm form);

    /**
     * Supprime un type de personne par son identifiant.
     *
     * @throws com.monentreprise.rh.exception.TypePersonneIntrouvableException si absent
     * @throws org.springframework.dao.DataIntegrityViolationException si utilisé par d'autres entités
     */
    void supprimer(Long id);

    /**
     * Convertit une entité vers un DTO de formulaire (utile pour pré-remplir le form).
     */
    TypePersonneForm versFormulaire(TypePersonne typePersonne);
}
