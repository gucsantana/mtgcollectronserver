package com.gucsantana.mtgcollectronserver.controllers;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.gucsantana.mtgcollectronserver.dto.LoginRequest;
import com.gucsantana.mtgcollectronserver.dto.RegisterUserRequest;
import com.gucsantana.mtgcollectronserver.models.User;
import com.gucsantana.mtgcollectronserver.repos.UserRepository;

import io.swagger.v3.oas.annotations.Operation;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    AuthController(
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
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

        return new LoginResponse(authentication.getName());
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

    @GetMapping("/session")
    public ResponseEntity<SessionResponse> checkValidSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SessionResponse(false, null));
        }

        return ResponseEntity.ok(new SessionResponse(true, authentication.getName()));
    }


    @PostMapping("/register")
    @Operation(summary = "Creates a user account by passing valid username, password and email fields")
    public UserResponse createNewAccount(@Valid @RequestBody RegisterUserRequest request) {
        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setUsername(request.username());
        newUser.setPasswordHash(passwordEncoder.encode(request.password()));

        LocalDateTime now = LocalDateTime.now();
        newUser.setCreatedAt(now);
        newUser.setUpdatedAt(now);

        UserResponse createdUser = toUserResponse(userRepository.save(newUser));
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

    public record LoginResponse(String username) {
    }

    public record SessionResponse(boolean authenticated, String username) {
    }
}
