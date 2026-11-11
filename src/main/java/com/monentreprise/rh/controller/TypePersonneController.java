package com.monentreprise.rh.controller;

import com.monentreprise.rh.dto.TypePersonneForm;
import com.monentreprise.rh.entity.TypePersonne;
import com.monentreprise.rh.exception.CodeDejaExistantException;
import com.monentreprise.rh.exception.TypePersonneIntrouvableException;
import com.monentreprise.rh.service.TypePersonneService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Contrôleur MVC pour la gestion des types de personne.
 * Pattern : Post-Redirect-Get (PRG).
 */
@Controller
@RequestMapping("/types-personne")
public class TypePersonneController {

    // Clés des attributs flash
    private static final String FLASH_SUCCES  = "flashSucces";
    private static final String FLASH_ERREUR  = "flashErreur";

    // Noms des vues
    private static final String VUE_LISTE      = "type-personne/liste";
    private static final String VUE_FORMULAIRE = "type-personne/formulaire";

    // Redirection de base
    private static final String REDIRECT_LISTE = "redirect:/types-personne";

    private static final int TAILLE_PAGE_DEFAUT = 10;

    private final TypePersonneService typePersonneService;

    public TypePersonneController(TypePersonneService typePersonneService) {
        this.typePersonneService = typePersonneService;
    }

    // =========================================================================
    // GET /types-personne — liste paginée avec recherche
    // =========================================================================

    @GetMapping
    public String afficherListe(
            @RequestParam(defaultValue = "") String motCle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille,
            Model model) {

        Pageable pageable = PageRequest.of(page, taille, Sort.by("code").ascending());
        Page<TypePersonne> resultats = typePersonneService.listerAvecRecherche(motCle, pageable);

        model.addAttribute("types", resultats);
        model.addAttribute("motCle", motCle);
        model.addAttribute("taille", taille);

        return VUE_LISTE;
    }

    // =========================================================================
    // GET /types-personne/nouveau — formulaire de création
    // =========================================================================

    @GetMapping("/nouveau")
    public String afficherFormulaireCreation(Model model) {
        model.addAttribute("typePersonneForm", new TypePersonneForm());
        model.addAttribute("modeCreation", true);
        return VUE_FORMULAIRE;
    }

    // =========================================================================
    // POST /types-personne — traitement de la création
    // =========================================================================

    @PostMapping
    public String creer(
            @Valid @ModelAttribute("typePersonneForm") TypePersonneForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("modeCreation", true);
            return VUE_FORMULAIRE;
        }

        try {
            typePersonneService.creer(form);
            redirectAttributes.addFlashAttribute(FLASH_SUCCES,
                    "Le type « " + form.getCode().toUpperCase() + " » a été créé avec succès.");
        } catch (CodeDejaExistantException e) {
            bindingResult.rejectValue("code", "code.existant", e.getMessage());
            model.addAttribute("modeCreation", true);
            return VUE_FORMULAIRE;
        }

        return REDIRECT_LISTE;
    }

    // =========================================================================
    // GET /types-personne/{id}/modifier — formulaire de modification
    // =========================================================================

    @GetMapping("/{id}/modifier")
    public String afficherFormulaireModification(@PathVariable Long id, Model model,
                                                  RedirectAttributes redirectAttributes) {
        try {
            TypePersonne typePersonne = typePersonneService.trouverParId(id);
            model.addAttribute("typePersonneForm", typePersonneService.versFormulaire(typePersonne));
            model.addAttribute("modeCreation", false);
            return VUE_FORMULAIRE;
        } catch (TypePersonneIntrouvableException e) {
            redirectAttributes.addFlashAttribute(FLASH_ERREUR, e.getMessage());
            return REDIRECT_LISTE;
        }
    }

    // =========================================================================
    // POST /types-personne/{id} — traitement de la modification
    // =========================================================================

    @PostMapping("/{id}")
    public String modifier(
            @PathVariable Long id,
            @Valid @ModelAttribute("typePersonneForm") TypePersonneForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("modeCreation", false);
            return VUE_FORMULAIRE;
        }

        try {
            typePersonneService.modifier(id, form);
            redirectAttributes.addFlashAttribute(FLASH_SUCCES,
                    "Le type « " + form.getCode().toUpperCase() + " » a été modifié avec succès.");
        } catch (CodeDejaExistantException e) {
            bindingResult.rejectValue("code", "code.existant", e.getMessage());
            model.addAttribute("modeCreation", false);
            return VUE_FORMULAIRE;
        } catch (TypePersonneIntrouvableException e) {
            redirectAttributes.addFlashAttribute(FLASH_ERREUR, e.getMessage());
        }

        return REDIRECT_LISTE;
    }

    // =========================================================================
    // POST /types-personne/{id}/supprimer — suppression
    // =========================================================================

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            TypePersonne typePersonne = typePersonneService.trouverParId(id);
            String codeSuppr = typePersonne.getCode();
            typePersonneService.supprimer(id);
            redirectAttributes.addFlashAttribute(FLASH_SUCCES,
                    "Le type « " + codeSuppr + " » a été supprimé avec succès.");
        } catch (TypePersonneIntrouvableException e) {
            redirectAttributes.addFlashAttribute(FLASH_ERREUR, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute(FLASH_ERREUR,
                    "Impossible de supprimer ce type : il est utilisé par d'autres données " +
                    "dans l'application. Veuillez d'abord le dissocier.");
        }

        return REDIRECT_LISTE;
    }
}
