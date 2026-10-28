package com.gucsantana.mtgcollectronserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import com.gucsantana.mtgcollectronserver.dto.RegisterUserRequest;
import com.gucsantana.mtgcollectronserver.models.User;
import com.gucsantana.mtgcollectronserver.repos.UserPreferencesRepository;
import com.gucsantana.mtgcollectronserver.repos.UserRepository;

class AuthControllerTest {

    @Test
    void createsDefaultPreferencesForNewAccount() {
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserPreferencesRepository userPreferencesRepository = mock(UserPreferencesRepository.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        SecurityContextRepository securityContextRepository = mock(SecurityContextRepository.class);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(42L);
                    return user;
                });

        AuthController controller = new AuthController(
                passwordEncoder,
                userRepository,
                userPreferencesRepository,
                authenticationManager,
                securityContextRepository);

        AuthController.UserResponse response = controller.createNewAccount(
                new RegisterUserRequest("user@example.com", "newuser", "password123"));

        assertEquals(42L, response.id());
        verify(userPreferencesRepository).save(argThat(preferences ->
                preferences.getUserId().equals(42L)
                        && preferences.getFullSetOptionSelected().equals(1)
                        && preferences.getCardPerPageOptionSelected().equals(60)
                        && Boolean.FALSE.equals(preferences.getDarkMode())
                        && Boolean.TRUE.equals(preferences.getShowPrices())
                        && Boolean.TRUE.equals(preferences.getShowSidebarPercent())));
    }
}
