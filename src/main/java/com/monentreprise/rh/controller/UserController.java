package com.monentreprise.rh.controller;

import com.monentreprise.rh.dto.UserForm;
import com.monentreprise.rh.entity.User;
import com.monentreprise.rh.exception.EmailDejaExistantException;
import com.monentreprise.rh.exception.UserIntrouvableException;
import com.monentreprise.rh.exception.UsernameDejaExistantException;
import com.monentreprise.rh.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Contrôleur CRUD pour la gestion des utilisateurs.
 * Réservé au rôle ADMIN (configuré dans SecurityConfig).
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private static final String FLASH_SUCCES  = "flashSucces";
    private static final String FLASH_ERREUR  = "flashErreur";
    private static final String VUE_LISTE     = "users/liste";
    private static final String VUE_FORM      = "users/formulaire";
    private static final String VUE_RESET_PWD = "users/reset-password";
    private static final String REDIRECT_LISTE = "redirect:/users";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =========================================================================
    // GET /users — liste
    // =========================================================================

    @GetMapping
    public String liste(
            @RequestParam(defaultValue = "") String motCle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille,
            Model model) {

        Pageable pageable = PageRequest.of(page, taille, Sort.by("username").ascending());
        Page<User> users = userService.listerAvecRecherche(motCle, pageable);

        model.addAttribute("users", users);
        model.addAttribute("motCle", motCle);
        model.addAttribute("taille", taille);
        return VUE_LISTE;
    }

    // =========================================================================
    // GET /users/nouveau
    // =========================================================================

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("userForm", new UserForm());
        model.addAttribute("roles", userService.listerTousLesRoles());
        model.addAttribute("modeCreation", true);
        return VUE_FORM;
    }

    // =========================================================================
    // POST /users — création
    // =========================================================================

    @PostMapping
    public String creer(
            @Valid @ModelAttribute("userForm") UserForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Mot de passe obligatoire à la création
        if (!StringUtils.hasText(form.getPassword())) {
            bindingResult.rejectValue("password", "required", "Le mot de passe est obligatoire.");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Les mots de passe ne correspondent pas.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", userService.listerTousLesRoles());
            model.addAttribute("modeCreation", true);
            return VUE_FORM;
        }

        try {
            userService.creer(form);
            redirectAttributes.addFlashAttribute(FLASH_SUCCES,
                    "L'utilisateur « " + form.getUsername() + " » a été créé.");
        } catch (UsernameDejaExistantException e) {
            bindingResult.rejectValue("username", "existant", e.getMessage());
            model.addAttribute("roles", userService.listerTousLesRoles());
            model.addAttribute("modeCreation", true);
            return VUE_FORM;
        } catch (EmailDejaExistantException e) {
            bindingResult.rejectValue("email", "existant", e.getMessage());
            model.addAttribute("roles", userService.listerTousLesRoles());
            model.addAttribute("modeCreation", true);
            return VUE_FORM;
        }
        return REDIRECT_LISTE;
    }

    // =========================================================================
    // GET /users/{id}/modifier
    // =========================================================================

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Long id, Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.trouverParId(id);
            model.addAttribute("userForm", userService.versFormulaire(user));
            model.addAttribute("roles", userService.listerTousLesRoles());
            model.addAttribute("modeCreation", false);
            return VUE_FORM;
        } catch (UserIntrouvableException e) {
            redirectAttributes.addFlashAttribute(FLASH_ERREUR, e.getMessage());
            return REDIRECT_LISTE;
        }
    }

    // =========================================================================
    // POST /users/{id} — mise à jour
    // =========================================================================

    @PostMapping("/{id}")
    public String mettreAJour(
            @PathVariable Long id,
            @Valid @ModelAttribute("userForm") UserForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // En modification, si un mot de passe est saisi on valide la confirmation
        if (StringUtils.hasText(form.getPassword()) &&
                !form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Les mots de passe ne correspondent pas.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", userService.listerTousLesRoles());
            model.addAttribute("modeCreation", false);
            return VUE_FORM;
        }

        try {
            userService.modifier(id, form);
            redirectAttributes.addFlashAttribute(FLASH_SUCCES,
                    "L'utilisateur « " + form.getUsername() + " » a été modifié.");
        } catch (UsernameDejaExistantException e) {
            bindingResult.rejectValue("username", "existant", e.getMessage());
            model.addAttribute("roles", userService.listerTousLesRoles());
            model.addAttribute("modeCreation", false);
            return VUE_FORM;
        } catch (EmailDejaExistantException | UserIntrouvableException e) {
            redirectAttributes.addFlashAttribute(FLASH_ERREUR, e.getMessage());
        }
        return REDIRECT_LISTE;
    }

    // =========================================================================
    // POST /users/{id}/supprimer
    // =========================================================================

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails connecte,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.supprimer(id, connecte.getUsername());
            redirectAttributes.addFlashAttribute(FLASH_SUCCES, "Utilisateur supprimé.");
        } catch (UserIntrouvableException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute(FLASH_ERREUR, e.getMessage());
        }
        return REDIRECT_LISTE;
    }

    // =========================================================================
    // GET /users/{id}/reset-password
    // =========================================================================

    @GetMapping("/{id}/reset-password")
    public String afficherResetPassword(@PathVariable Long id, Model model,
                                         RedirectAttributes redirectAttributes) {
        try {
            userService.trouverParId(id);
            model.addAttribute("userId", id);
            return VUE_RESET_PWD;
        } catch (UserIntrouvableException e) {
            redirectAttributes.addFlashAttribute(FLASH_ERREUR, e.getMessage());
            return REDIRECT_LISTE;
        }
    }

    // =========================================================================
    // POST /users/{id}/reset-password
    // =========================================================================

    @PostMapping("/{id}/reset-password")
    public String reinitialiserMotDePasse(
            @PathVariable Long id,
            @RequestParam String nouveauMotDePasse,
            @RequestParam String confirmMotDePasse,
            @AuthenticationPrincipal UserDetails connecte,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!nouveauMotDePasse.equals(confirmMotDePasse)) {
            model.addAttribute("userId", id);
            model.addAttribute("erreurPwd", "Les mots de passe ne correspondent pas.");
            return VUE_RESET_PWD;
        }

        try {
            userService.reinitialiserMotDePasse(id, nouveauMotDePasse, connecte.getUsername());
            redirectAttributes.addFlashAttribute(FLASH_SUCCES, "Mot de passe réinitialisé avec succès.");
        } catch (IllegalArgumentException | UserIntrouvableException e) {
            redirectAttributes.addFlashAttribute(FLASH_ERREUR, e.getMessage());
        }
        return REDIRECT_LISTE;
    }
}
