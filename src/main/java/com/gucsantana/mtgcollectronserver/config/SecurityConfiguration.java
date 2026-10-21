package com.gucsantana.mtgcollectronserver.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gucsantana.mtgcollectronserver.repos.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(user -> User.withUsername(user.getUsername())
                        .password(user.getPasswordHash())
                        .authorities("ROLE_USER")
                        .disabled(!Boolean.TRUE.equals(user.getIsActive()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository) throws Exception {
        http
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository))
                .csrf(csrf -> csrf.ignoringRequestMatchers(request ->
                        HttpMethod.POST.matches(request.getMethod())
                                && "/auth/register".equals(request.getServletPath()))
                        .withObjectPostProcessor(new ObjectPostProcessor<CsrfFilter>() {
                            @Override
                                    public CsrfFilter postProcess(CsrfFilter filter) {
                                filter.setAccessDeniedHandler(SecurityConfiguration::handleCsrfAccessDenied);
                                return filter;
                            }
                        }))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/session").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                writeSecurityError(response, HttpStatus.UNAUTHORIZED,
                                        "AUTHENTICATION_REQUIRED",
                                        "You must log in to access this resource."))
                        .accessDeniedHandler((request, response, exception) ->
                                writeSecurityError(response, HttpStatus.FORBIDDEN,
                                        "ACCESS_DENIED",
                                        "You do not have permission to access this resource.")));

        return http.build();
    }

    private static void handleCsrfAccessDenied(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        if (exception instanceof CsrfException) {
            boolean missingToken = exception instanceof MissingCsrfTokenException;
            LOGGER.warn("CSRF validation failed for {} {}: {}",
                    request.getMethod(), request.getRequestURI(),
                    missingToken ? "token missing" : "token invalid");
            writeSecurityError(response, HttpStatus.FORBIDDEN,
                    missingToken ? "CSRF_TOKEN_MISSING" : "CSRF_TOKEN_INVALID",
                    missingToken
                            ? "A CSRF token is required. Fetch /auth/csrf with your session and send the returned token in its named header."
                            : "The CSRF token is invalid or does not match your session. Fetch a new token from /auth/csrf using the same session.");
            return;
        }

        writeSecurityError(response, HttpStatus.FORBIDDEN,
                "ACCESS_DENIED", "You do not have permission to access this resource.");
    }

    private static void writeSecurityError(
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":" + status.value()
                + ",\"code\":\"" + code
                + "\",\"message\":\"" + message + "\"}");
    }
}
