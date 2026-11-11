package com.monentreprise.rh.controller;

import com.monentreprise.rh.dto.RegisterForm;
import com.monentreprise.rh.exception.EmailDejaExistantException;
import com.monentreprise.rh.exception.UsernameDejaExistantException;
import com.monentreprise.rh.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Contrôleur pour l'authentification : login et inscription.
 */
@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // -------------------------------------------------------------------------
    // GET /login
    // -------------------------------------------------------------------------

    @GetMapping("/login")
    public String afficherLogin() {
        return "auth/login";
    }

    // -------------------------------------------------------------------------
    // GET /register
    // -------------------------------------------------------------------------

    @GetMapping("/register")
    public String afficherInscription(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    // -------------------------------------------------------------------------
    // POST /register
    // -------------------------------------------------------------------------

    @PostMapping("/register")
    public String inscrire(
            @Valid @ModelAttribute("registerForm") RegisterForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // Validation : confirmation du mot de passe
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch",
                    "Les mots de passe ne correspondent pas.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.inscrire(form);
            redirectAttributes.addFlashAttribute("flashSucces",
                    "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/login?registered";
        } catch (UsernameDejaExistantException e) {
            bindingResult.rejectValue("username", "username.existant", e.getMessage());
            return "auth/register";
        } catch (EmailDejaExistantException e) {
            bindingResult.rejectValue("email", "email.existant", e.getMessage());
            return "auth/register";
        }
    }
}
