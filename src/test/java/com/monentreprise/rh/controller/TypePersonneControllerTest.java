package com.monentreprise.rh.controller;

import com.monentreprise.rh.dto.TypePersonneForm;
import com.monentreprise.rh.entity.TypePersonne;
import com.monentreprise.rh.exception.CodeDejaExistantException;
import com.monentreprise.rh.exception.TypePersonneIntrouvableException;
import com.monentreprise.rh.service.TypePersonneService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests du contrôleur TypePersonne avec Spring Security activé.
 */
@WebMvcTest(TypePersonneController.class)
@Import(com.monentreprise.rh.config.SecurityConfig.class)
class TypePersonneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private TypePersonneService typePersonneService;
    @MockBean private com.monentreprise.rh.security.CustomUserDetailsService customUserDetailsService;

    // -------------------------------------------------------------------------
    // GET /types-personne
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("GET /types-personne — retourne la vue liste avec le modèle")
    void getListe_retourneVueListe() throws Exception {
        TypePersonne typeEmp = new TypePersonne("EMP", "Employé");
        typeEmp.setId(1L);

        given(typePersonneService.listerAvecRecherche(eq(""), any()))
                .willReturn(new PageImpl<>(List.of(typeEmp), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/types-personne"))
                .andExpect(status().isOk())
                .andExpect(view().name("type-personne/liste"))
                .andExpect(model().attributeExists("types", "motCle"));
    }

    // -------------------------------------------------------------------------
    // GET /types-personne/nouveau
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("GET /types-personne/nouveau — retourne le formulaire vide")
    void getNouveauFormulaire_retourneFormVide() throws Exception {
        mockMvc.perform(get("/types-personne/nouveau"))
                .andExpect(status().isOk())
                .andExpect(view().name("type-personne/formulaire"))
                .andExpect(model().attributeExists("typePersonneForm"))
                .andExpect(model().attribute("modeCreation", true));
    }

    // -------------------------------------------------------------------------
    // POST /types-personne — création réussie
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("POST /types-personne — données valides redirige vers la liste")
    void postCreer_donnéesValides_redirige() throws Exception {
        TypePersonne typeEmp = new TypePersonne("EMP", "Employé");
        typeEmp.setId(1L);
        given(typePersonneService.creer(any(TypePersonneForm.class))).willReturn(typeEmp);

        mockMvc.perform(post("/types-personne").with(csrf())
                        .param("code", "EMP")
                        .param("designation", "Employé"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/types-personne"))
                .andExpect(flash().attributeExists("flashSucces"));
    }

    // -------------------------------------------------------------------------
    // POST /types-personne — erreur de validation (champ vide)
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("POST /types-personne — code vide retourne le formulaire avec erreurs")
    void postCreer_codeVide_retourneFormAvecErreurs() throws Exception {
        mockMvc.perform(post("/types-personne").with(csrf())
                        .param("code", "")
                        .param("designation", "Employé"))
                .andExpect(status().isOk())
                .andExpect(view().name("type-personne/formulaire"))
                .andExpect(model().attributeHasFieldErrors("typePersonneForm", "code"));
    }

    // -------------------------------------------------------------------------
    // POST /types-personne — code déjà existant
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("POST /types-personne — code existant retourne le formulaire avec message")
    void postCreer_codeExistant_retourneFormAvecErreur() throws Exception {
        given(typePersonneService.creer(any(TypePersonneForm.class)))
                .willThrow(new CodeDejaExistantException("EMP"));

        mockMvc.perform(post("/types-personne").with(csrf())
                        .param("code", "EMP")
                        .param("designation", "Employé"))
                .andExpect(status().isOk())
                .andExpect(view().name("type-personne/formulaire"))
                .andExpect(model().attributeHasFieldErrors("typePersonneForm", "code"));
    }

    // -------------------------------------------------------------------------
    // GET /types-personne/{id}/modifier
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("GET /types-personne/{id}/modifier — id existant retourne le formulaire pré-rempli")
    void getModifier_idExistant_retourneFormPreRempli() throws Exception {
        TypePersonne typeEmp = new TypePersonne("EMP", "Employé");
        typeEmp.setId(1L);
        TypePersonneForm form = new TypePersonneForm(1L, "EMP", "Employé");

        given(typePersonneService.trouverParId(1L)).willReturn(typeEmp);
        given(typePersonneService.versFormulaire(typeEmp)).willReturn(form);

        mockMvc.perform(get("/types-personne/1/modifier"))
                .andExpect(status().isOk())
                .andExpect(view().name("type-personne/formulaire"))
                .andExpect(model().attribute("modeCreation", false));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /types-personne/{id}/modifier — id absent redirige avec flashErreur")
    void getModifier_idAbsent_redirigeSurListe() throws Exception {
        given(typePersonneService.trouverParId(99L))
                .willThrow(new TypePersonneIntrouvableException(99L));

        mockMvc.perform(get("/types-personne/99/modifier"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/types-personne"))
                .andExpect(flash().attributeExists("flashErreur"));
    }

    // -------------------------------------------------------------------------
    // POST /types-personne/{id}/supprimer
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser
    @DisplayName("POST /types-personne/{id}/supprimer — succès redirige avec flashSucces")
    void postSupprimer_succes_redirige() throws Exception {
        TypePersonne typeEmp = new TypePersonne("EMP", "Employé");
        typeEmp.setId(1L);
        given(typePersonneService.trouverParId(1L)).willReturn(typeEmp);
        willDoNothing().given(typePersonneService).supprimer(1L);

        mockMvc.perform(post("/types-personne/1/supprimer").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/types-personne"))
                .andExpect(flash().attributeExists("flashSucces"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /types-personne/{id}/supprimer — sans CSRF → 403")
    void postSupprimer_sansCsrf_retourne403() throws Exception {
        // Sans .with(csrf()), Spring Security bloque avec 403
        mockMvc.perform(post("/types-personne/1/supprimer"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /types-personne/{id} — modification réussie redirige")
    void postModifier_succes_redirige() throws Exception {
        TypePersonne typeEmp = new TypePersonne("EMP", "Employé permanent");
        typeEmp.setId(1L);
        given(typePersonneService.modifier(eq(1L), any(TypePersonneForm.class))).willReturn(typeEmp);

        mockMvc.perform(post("/types-personne/1").with(csrf())
                        .param("code", "EMP")
                        .param("designation", "Employé permanent"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/types-personne"))
                .andExpect(flash().attributeExists("flashSucces"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /types-personne/{id} — sans CSRF → 403")
    void postModifier_sansCsrf_retourne403() throws Exception {
        mockMvc.perform(post("/types-personne/1")
                        .param("code", "EMP")
                        .param("designation", "Employé permanent"))
                .andExpect(status().isForbidden());
    }
}
