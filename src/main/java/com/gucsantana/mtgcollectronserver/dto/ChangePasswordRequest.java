package com.gucsantana.mtgcollectronserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotNull @Positive Long userId,
        @NotBlank @Size(min = 8, max = 72) String currentPassword,
        @NotBlank @Size(min = 8, max = 72) String desiredPassword) {
}
