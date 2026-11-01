package com.gucsantana.mtgcollectronserver.dto;

import jakarta.validation.constraints.NotBlank;

public record CardsByNameInSetRequest(
        @NotBlank String set,
        @NotBlank String name) {
}
