package com.monentreprise.rh.service.impl;

import com.monentreprise.rh.dto.TypePersonneForm;
import com.monentreprise.rh.entity.TypePersonne;
import com.monentreprise.rh.exception.CodeDejaExistantException;
import com.monentreprise.rh.exception.TypePersonneIntrouvableException;
import com.monentreprise.rh.repository.TypePersonneRepository;
import com.monentreprise.rh.service.TypePersonneService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Implémentation du service de gestion des types de personne.
 */
@Service
@Transactional
public class TypePersonneServiceImpl implements TypePersonneService {

    private final TypePersonneRepository typePersonneRepository;

    public TypePersonneServiceImpl(TypePersonneRepository typePersonneRepository) {
        this.typePersonneRepository = typePersonneRepository;
    }

    // -------------------------------------------------------------------------
    // Lecture
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<TypePersonne> listerAvecRecherche(String motCle, Pageable pageable) {
        if (StringUtils.hasText(motCle)) {
            return typePersonneRepository.rechercherParMotCle(motCle.trim(), pageable);
        }
        return typePersonneRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public TypePersonne trouverParId(Long id) {
        return typePersonneRepository.findById(id)
                .orElseThrow(() -> new TypePersonneIntrouvableException(id));
    }

    // -------------------------------------------------------------------------
    // Écriture
    // -------------------------------------------------------------------------

    @Override
    public TypePersonne creer(TypePersonneForm form) {
        // Vérification d'unicité du code
        if (typePersonneRepository.existsByCodeIgnoreCase(form.getCode())) {
            throw new CodeDejaExistantException(form.getCode());
        }

        TypePersonne typePersonne = new TypePersonne();
        typePersonne.setCode(form.getCode().toUpperCase());
        typePersonne.setDesignation(form.getDesignation());

        return typePersonneRepository.save(typePersonne);
    }

    @Override
    public TypePersonne modifier(Long id, TypePersonneForm form) {
        TypePersonne existant = trouverParId(id);

        // Vérifie qu'aucun AUTRE enregistrement n'utilise déjà ce code
        if (typePersonneRepository.existsByCodeIgnoreCaseAndIdNot(form.getCode(), id)) {
            throw new CodeDejaExistantException(form.getCode());
        }

        existant.setCode(form.getCode().toUpperCase());
        existant.setDesignation(form.getDesignation());

        return typePersonneRepository.save(existant);
    }

    @Override
    public void supprimer(Long id) {
        // S'assure que l'entité existe avant la suppression
        TypePersonne typePersonne = trouverParId(id);
        // La DataIntegrityViolationException (FK) remontera naturellement
        // et sera interceptée dans le contrôleur
        typePersonneRepository.delete(typePersonne);
        typePersonneRepository.flush(); // force le flush pour détecter la contrainte immédiatement
    }

    // -------------------------------------------------------------------------
    // Utilitaire de conversion
    // -------------------------------------------------------------------------

    @Override
    public TypePersonneForm versFormulaire(TypePersonne typePersonne) {
        return new TypePersonneForm(
                typePersonne.getId(),
                typePersonne.getCode(),
                typePersonne.getDesignation()
        );
    }
}
