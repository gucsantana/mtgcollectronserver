package com.gucsantana.mtgcollectronserver.controllers;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.dto.ChangePasswordRequest;
import com.gucsantana.mtgcollectronserver.dto.DeleteAccountRequest;
import com.gucsantana.mtgcollectronserver.dto.LoginRequest;
import com.gucsantana.mtgcollectronserver.dto.RegisterUserRequest;
import com.gucsantana.mtgcollectronserver.models.User;
import com.gucsantana.mtgcollectronserver.models.UserPreferences;
import com.gucsantana.mtgcollectronserver.repos.OwnedCardRepository;
import com.gucsantana.mtgcollectronserver.repos.UserRepository;
import com.gucsantana.mtgcollectronserver.repos.UserPreferencesRepository;
import com.gucsantana.mtgcollectronserver.repos.UserSetInfoRepository;

import io.swagger.v3.oas.annotations.Operation;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final OwnedCardRepository ownedCardRepository;
    private final UserSetInfoRepository userSetInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    AuthController(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            UserPreferencesRepository userPreferencesRepository,
            OwnedCardRepository ownedCardRepository,
            UserSetInfoRepository userSetInfoRepository,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.ownedCardRepository = ownedCardRepository;
        this.userSetInfoRepository = userSetInfoRepository;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/csrf")
    public CsrfToken csrfToken(CsrfToken csrfToken) {
        return csrfToken;
    }

    @PostMapping("/login")
    @Operation(summary = "Attempts to log in user with username and password credentials")
    public LoginResponse loginUser(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username(), request.password()));
        } catch (AuthenticationException exception) {
            LOGGER.warn("Invalid username or password for user "+ request.username());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        if (httpRequest.getSession(false) != null) {
            httpRequest.changeSessionId();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
        return new LoginResponse(user.getId(), authentication.getName());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logs out the current user by invalidating the session")
    public ResponseEntity<Void> logoutUser(HttpServletRequest httpRequest) {
        SecurityContextHolder.clearContext();

        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Changes a user's password after verifying the current password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User user = findUserForPasswordOperation(request.userId(), request.currentPassword());
        user.setPasswordHash(passwordEncoder.encode(request.desiredPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete-account")
    @Transactional
    @CacheEvict(value = {
            "user-cache",
            "user-preferences-cache",
            "owned-card-cache",
            "user-set-info-cache"
    }, allEntries = true)
    @Operation(summary = "Deletes a user's account and all associated data")
    public ResponseEntity<Void> deleteAccount(
            @Valid @RequestBody DeleteAccountRequest request,
            HttpServletRequest httpRequest) {
        User user = findUserForPasswordOperation(request.userId(), request.password());

        ownedCardRepository.deleteByCardOwnerId(user.getId());
        userSetInfoRepository.deleteByCardOwnerId(user.getId());
        userPreferencesRepository.deleteById(user.getId());
        userRepository.delete(user);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && user.getUsername().equals(authentication.getName())) {
            SecurityContextHolder.clearContext();
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }

        LOGGER.info("Deleted account for user {}", user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/session")
    public ResponseEntity<SessionResponse> checkValidSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SessionResponse(false, null, null));
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
        return ResponseEntity.ok(new SessionResponse(true, user.getId(), authentication.getName()));
    }

    private User findUserForPasswordOperation(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with ID: " + userId));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }
        return user;
    }

    @PostMapping("/register")
    @Operation(summary = "Creates a user account by passing valid username, password and email fields")
    @Transactional
    @CacheEvict(value = "user-preferences-cache", allEntries = true)
    public UserResponse createNewAccount(@Valid @RequestBody RegisterUserRequest request) {
        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setUsername(request.username());
        newUser.setPasswordHash(passwordEncoder.encode(request.password()));

        LocalDateTime now = LocalDateTime.now();
        newUser.setCreatedAt(now);
        newUser.setUpdatedAt(now);

        User savedUser = userRepository.save(newUser);

        UserPreferences preferences = new UserPreferences();
        preferences.setUserId(savedUser.getId());
        preferences.setFullSetOptionSelected(1);
        preferences.setCardPerPageOptionSelected(1);
        preferences.setDarkMode(false);
        preferences.setShowPrices(true);
        preferences.setShowSidebarPercent(true);
        userPreferencesRepository.save(preferences);

        UserResponse createdUser = toUserResponse(savedUser);
        LOGGER.info("Created new account for user "+ newUser.getUsername() + ", with email " + newUser.getEmail() );
        return createdUser;
    }

    private static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getEmailVerifiedAt(),
                user.getIsActive(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public record UserResponse(
            Long id,
            String email,
            String username,
            LocalDateTime emailVerifiedAt,
            Boolean isActive,
            LocalDateTime lastLoginAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
    }

    public record LoginResponse(Long userId, String username) {
    }

    public record SessionResponse(boolean authenticated, Long userId, String username) {
    }
}
