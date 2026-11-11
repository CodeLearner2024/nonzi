package com.monentreprise.rh.security;

import com.monentreprise.rh.controller.AuthController;
import com.monentreprise.rh.controller.DashboardController;
import com.monentreprise.rh.controller.UserController;
import com.monentreprise.rh.service.AuthService;
import com.monentreprise.rh.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de contrôle d'accès Spring Security.
 */
@WebMvcTest(controllers = {DashboardController.class, UserController.class, AuthController.class})
@Import(com.monentreprise.rh.config.SecurityConfig.class)
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UserService    userService;
    @MockBean private AuthService    authService;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    // -------------------------------------------------------------------------
    // Non authentifié → redirection vers /login
    // -------------------------------------------------------------------------

    @Test
    @WithAnonymousUser
    @DisplayName("Anonyme sur /dashboard → redirection vers /login")
    void anonyme_dashboard_redirigeSurLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Anonyme sur /users → redirection vers /login")
    void anonyme_users_redirigeSurLogin() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // -------------------------------------------------------------------------
    // ROLE_USER sur /users → 403
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    @DisplayName("ROLE_USER sur /users → 403 Forbidden")
    void roleUser_surUsers_retourne403() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // ROLE_ADMIN sur /users → 200
    // -------------------------------------------------------------------------

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("ROLE_ADMIN sur /users → 200 OK")
    void roleAdmin_surUsers_retourne200() throws Exception {
        given(userService.listerAvecRecherche(eq(""), any()))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/liste"));
    }

    // -------------------------------------------------------------------------
    // Pages publiques accessibles sans authentification
    // -------------------------------------------------------------------------

    @Test
    @WithAnonymousUser
    @DisplayName("Anonyme sur /login → 200 OK")
    void anonyme_login_retourne200() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Anonyme sur /register → 200 OK")
    void anonyme_register_retourne200() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }
}
