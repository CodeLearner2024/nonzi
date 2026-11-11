package com.monentreprise.rh.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur du tableau de bord — page d'accueil après connexion.
 */
@Controller
public class DashboardController {

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("nomUtilisateur", userDetails.getUsername());
        return "dashboard/index";
    }
}
