package com.monentreprise.rh.repository;

import com.monentreprise.rh.entity.TypePersonne;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository Spring Data JPA pour l'entité TypePersonne.
 */
@Repository
public interface TypePersonneRepository extends JpaRepository<TypePersonne, Long> {

    /**
     * Vérifie si un code existe déjà (insensible à la casse) — pour la création.
     */
    boolean existsByCodeIgnoreCase(String code);

    /**
     * Vérifie si un code existe pour un autre enregistrement — pour la modification.
     *
     * @param code le code à vérifier
     * @param id   l'id de l'enregistrement à exclure de la recherche
     */
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    /**
     * Recherche paginée : retourne les types dont le code OU la désignation
     * contient le mot-clé (insensible à la casse).
     *
     * @param motCle   fragment de recherche
     * @param pageable paramètres de pagination et de tri
     */
    @Query("SELECT t FROM TypePersonne t " +
           "WHERE LOWER(t.code) LIKE LOWER(CONCAT('%', :motCle, '%')) " +
           "   OR LOWER(t.designation) LIKE LOWER(CONCAT('%', :motCle, '%'))")
    Page<TypePersonne> rechercherParMotCle(@Param("motCle") String motCle, Pageable pageable);
}
