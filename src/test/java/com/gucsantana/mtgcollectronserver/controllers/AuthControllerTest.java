package com.gucsantana.mtgcollectronserver.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

import com.gucsantana.mtgcollectronserver.dto.ChangePasswordRequest;
import com.gucsantana.mtgcollectronserver.dto.DeleteAccountRequest;
import com.gucsantana.mtgcollectronserver.dto.RegisterUserRequest;
import com.gucsantana.mtgcollectronserver.models.User;
import com.gucsantana.mtgcollectronserver.repos.OwnedCardRepository;
import com.gucsantana.mtgcollectronserver.repos.UserPreferencesRepository;
import com.gucsantana.mtgcollectronserver.repos.UserRepository;
import com.gucsantana.mtgcollectronserver.repos.UserSetInfoRepository;

class AuthControllerTest {

    @Test
    void changesPasswordAfterVerifyingCurrentPassword() {
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setId(42L);
        user.setPasswordHash("old-hash");
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("desired-password")).thenReturn("new-hash");

        AuthController controller = createController(passwordEncoder, userRepository);
        var response = controller.changePassword(new ChangePasswordRequest(
                42L, "current-password", "desired-password"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new-hash", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void rejectsPasswordChangeWhenCurrentPasswordDoesNotMatch() {
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setId(42L);
        user.setPasswordHash("old-hash");
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

        AuthController controller = createController(passwordEncoder, userRepository);
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.changePassword(new ChangePasswordRequest(
                        42L, "wrong-password", "desired-password")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void deletesAccountDataBeforeDeletingUser() {
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserPreferencesRepository userPreferencesRepository = mock(UserPreferencesRepository.class);
        OwnedCardRepository ownedCardRepository = mock(OwnedCardRepository.class);
        UserSetInfoRepository userSetInfoRepository = mock(UserSetInfoRepository.class);
        User user = new User();
        user.setId(42L);
        user.setUsername("account-owner");
        user.setPasswordHash("password-hash");
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-password", "password-hash")).thenReturn(true);

        AuthController controller = new AuthController(
                passwordEncoder,
                userRepository,
                userPreferencesRepository,
                ownedCardRepository,
                userSetInfoRepository,
                mock(AuthenticationManager.class),
                mock(SecurityContextRepository.class));
        var response = controller.deleteAccount(
                new DeleteAccountRequest(42L, "current-password"),
                mock(HttpServletRequest.class));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        InOrder deletionOrder = inOrder(
                ownedCardRepository,
                userSetInfoRepository,
                userPreferencesRepository,
                userRepository);
        deletionOrder.verify(ownedCardRepository).deleteByCardOwnerId(42L);
        deletionOrder.verify(userSetInfoRepository).deleteByCardOwnerId(42L);
        deletionOrder.verify(userPreferencesRepository).deleteById(42L);
        deletionOrder.verify(userRepository).delete(user);
    }

    @Test
    void createsDefaultPreferencesForNewAccount() {
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserPreferencesRepository userPreferencesRepository = mock(UserPreferencesRepository.class);
        OwnedCardRepository ownedCardRepository = mock(OwnedCardRepository.class);
        UserSetInfoRepository userSetInfoRepository = mock(UserSetInfoRepository.class);
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
                ownedCardRepository,
                userSetInfoRepository,
                authenticationManager,
                securityContextRepository);

        AuthController.UserResponse response = controller.createNewAccount(
                new RegisterUserRequest("user@example.com", "newuser", "password123"));

        assertEquals(42L, response.id());
        verify(userPreferencesRepository).save(argThat(preferences ->
                preferences.getUserId().equals(42L)
                        && preferences.getFullSetOptionSelected().equals(1)
                        && preferences.getCardPerPageOptionSelected().equals(1)
                        && Boolean.FALSE.equals(preferences.getDarkMode())
                        && Boolean.TRUE.equals(preferences.getShowPrices())
                        && Boolean.TRUE.equals(preferences.getShowSidebarPercent())));
    }

    private static AuthController createController(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository) {
        return new AuthController(
                        passwordEncoder,
                        userRepository,
                        mock(UserPreferencesRepository.class),
                        mock(OwnedCardRepository.class),
                        mock(UserSetInfoRepository.class),
                        mock(AuthenticationManager.class),
                        mock(SecurityContextRepository.class));
    }
}
