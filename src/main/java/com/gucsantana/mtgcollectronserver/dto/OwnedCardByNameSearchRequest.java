package com.gucsantana.mtgcollectronserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OwnedCardByNameSearchRequest(
        @NotNull @JsonProperty("card_owner_id") Long cardOwnerId,
        @NotBlank @JsonProperty("card_name") String cardName) {
}
