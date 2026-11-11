package com.monentreprise.rh.security;

import com.monentreprise.rh.dto.RegisterForm;
import com.monentreprise.rh.entity.Role;
import com.monentreprise.rh.entity.User;
import com.monentreprise.rh.exception.EmailDejaExistantException;
import com.monentreprise.rh.exception.UsernameDejaExistantException;
import com.monentreprise.rh.repository.RoleRepository;
import com.monentreprise.rh.repository.UserRepository;
import com.monentreprise.rh.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Tests unitaires du service d'inscription.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository  userRepository;
    @Mock private RoleRepository  roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterForm formValide;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        formValide = new RegisterForm();
        formValide.setUsername("alice");
        formValide.setEmail("alice@rh.local");
        formValide.setPassword("Passw0rd!");
        formValide.setConfirmPassword("Passw0rd!");

        roleUser = new Role("ROLE_USER");
        roleUser.setId(2L);
    }

    @Test
    @DisplayName("inscrire — données valides crée l'utilisateur avec ROLE_USER")
    void inscrire_donneesValides_creeLUtilisateur() {
        given(userRepository.existsByUsername("alice")).willReturn(false);
        given(userRepository.existsByEmail("alice@rh.local")).willReturn(false);
        given(roleRepository.findByName("ROLE_USER")).willReturn(Optional.of(roleUser));
        given(passwordEncoder.encode("Passw0rd!")).willReturn("$2a$hashed");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        User result = authService.inscrire(formValide);

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getPassword()).isEqualTo("$2a$hashed"); // hashé, pas en clair
        assertThat(result.getRoles()).contains(roleUser);
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("inscrire — username déjà pris lève UsernameDejaExistantException")
    void inscrire_usernameExistant_leveException() {
        given(userRepository.existsByUsername("alice")).willReturn(true);

        assertThatThrownBy(() -> authService.inscrire(formValide))
                .isInstanceOf(UsernameDejaExistantException.class)
                .hasMessageContaining("alice");

        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("inscrire — email déjà pris lève EmailDejaExistantException")
    void inscrire_emailExistant_leveException() {
        given(userRepository.existsByUsername("alice")).willReturn(false);
        given(userRepository.existsByEmail("alice@rh.local")).willReturn(true);

        assertThatThrownBy(() -> authService.inscrire(formValide))
                .isInstanceOf(EmailDejaExistantException.class)
                .hasMessageContaining("alice@rh.local");

        then(userRepository).should(never()).save(any());
    }
}
