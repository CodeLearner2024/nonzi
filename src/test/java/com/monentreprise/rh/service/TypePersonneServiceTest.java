package com.monentreprise.rh.service;

import com.monentreprise.rh.dto.TypePersonneForm;
import com.monentreprise.rh.entity.TypePersonne;
import com.monentreprise.rh.exception.CodeDejaExistantException;
import com.monentreprise.rh.exception.TypePersonneIntrouvableException;
import com.monentreprise.rh.repository.TypePersonneRepository;
import com.monentreprise.rh.service.impl.TypePersonneServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Tests unitaires du service TypePersonne avec Mockito.
 */
@ExtendWith(MockitoExtension.class)
class TypePersonneServiceTest {

    @Mock
    private TypePersonneRepository typePersonneRepository;

    @InjectMocks
    private TypePersonneServiceImpl typePersonneService;

    private TypePersonne typeEmp;

    @BeforeEach
    void setUp() {
        typeEmp = new TypePersonne("EMP", "Employé");
        typeEmp.setId(1L);
    }

    // -------------------------------------------------------------------------
    // listerAvecRecherche
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listerAvecRecherche — sans mot-clé appelle findAll")
    void listerSansMotCle_doitAppelerFindAll() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<TypePersonne> pageMock = new PageImpl<>(List.of(typeEmp));
        given(typePersonneRepository.findAll(pageable)).willReturn(pageMock);

        Page<TypePersonne> resultat = typePersonneService.listerAvecRecherche("", pageable);

        assertThat(resultat.getContent()).hasSize(1);
        then(typePersonneRepository).should().findAll(pageable);
        then(typePersonneRepository).should(never()).rechercherParMotCle(any(), any());
    }

    @Test
    @DisplayName("listerAvecRecherche — avec mot-clé appelle rechercherParMotCle")
    void listerAvecMotCle_doitAppelerRecherche() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<TypePersonne> pageMock = new PageImpl<>(List.of(typeEmp));
        given(typePersonneRepository.rechercherParMotCle(eq("emp"), eq(pageable))).willReturn(pageMock);

        Page<TypePersonne> resultat = typePersonneService.listerAvecRecherche("emp", pageable);

        assertThat(resultat.getContent()).hasSize(1);
        then(typePersonneRepository).should().rechercherParMotCle("emp", pageable);
    }

    // -------------------------------------------------------------------------
    // trouverParId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("trouverParId — existant retourne le type")
    void trouverParId_existant_retourneLeType() {
        given(typePersonneRepository.findById(1L)).willReturn(Optional.of(typeEmp));

        TypePersonne resultat = typePersonneService.trouverParId(1L);

        assertThat(resultat.getCode()).isEqualTo("EMP");
    }

    @Test
    @DisplayName("trouverParId — absent lève TypePersonneIntrouvableException")
    void trouverParId_absent_leveException() {
        given(typePersonneRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> typePersonneService.trouverParId(99L))
                .isInstanceOf(TypePersonneIntrouvableException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // creer
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("creer — code nouveau sauvegarde et retourne le type")
    void creer_codeNouveauSauvegarde() {
        TypePersonneForm form = new TypePersonneForm(null, "STG", "Stagiaire");
        given(typePersonneRepository.existsByCodeIgnoreCase("STG")).willReturn(false);
        given(typePersonneRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        TypePersonne resultat = typePersonneService.creer(form);

        assertThat(resultat.getCode()).isEqualTo("STG");
        assertThat(resultat.getDesignation()).isEqualTo("Stagiaire");
    }

    @Test
    @DisplayName("creer — code existant lève CodeDejaExistantException")
    void creer_codeExistant_leveException() {
        TypePersonneForm form = new TypePersonneForm(null, "EMP", "Employé");
        given(typePersonneRepository.existsByCodeIgnoreCase("EMP")).willReturn(true);

        assertThatThrownBy(() -> typePersonneService.creer(form))
                .isInstanceOf(CodeDejaExistantException.class)
                .hasMessageContaining("EMP");

        then(typePersonneRepository).should(never()).save(any());
    }

    // -------------------------------------------------------------------------
    // modifier
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("modifier — données valides met à jour le type")
    void modifier_valeursValides_metsAJour() {
        TypePersonneForm form = new TypePersonneForm(1L, "EMP", "Employé permanent");
        given(typePersonneRepository.findById(1L)).willReturn(Optional.of(typeEmp));
        given(typePersonneRepository.existsByCodeIgnoreCaseAndIdNot("EMP", 1L)).willReturn(false);
        given(typePersonneRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        TypePersonne resultat = typePersonneService.modifier(1L, form);

        assertThat(resultat.getDesignation()).isEqualTo("Employé permanent");
    }

    @Test
    @DisplayName("modifier — code pris par autre lève CodeDejaExistantException")
    void modifier_codeDejaUtiliseParAutre_leveException() {
        TypePersonneForm form = new TypePersonneForm(1L, "STG", "Stagiaire");
        given(typePersonneRepository.findById(1L)).willReturn(Optional.of(typeEmp));
        given(typePersonneRepository.existsByCodeIgnoreCaseAndIdNot("STG", 1L)).willReturn(true);

        assertThatThrownBy(() -> typePersonneService.modifier(1L, form))
                .isInstanceOf(CodeDejaExistantException.class);
    }

    // -------------------------------------------------------------------------
    // versFormulaire
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("versFormulaire — convertit correctement l'entité en DTO")
    void versFormulaire_convertitCorrectement() {
        TypePersonneForm form = typePersonneService.versFormulaire(typeEmp);

        assertThat(form.getId()).isEqualTo(1L);
        assertThat(form.getCode()).isEqualTo("EMP");
        assertThat(form.getDesignation()).isEqualTo("Employé");
    }
}
