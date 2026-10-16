package com.gucsantana.mtgcollectronserver;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// holds data from new account creation requests
public record RegisterUserRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 6, max = 100) String username,
        @NotBlank @Size(min = 8, max = 72) String password) {
}
