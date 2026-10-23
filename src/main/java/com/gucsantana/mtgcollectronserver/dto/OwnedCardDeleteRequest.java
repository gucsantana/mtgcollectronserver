package com.gucsantana.mtgcollectronserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OwnedCardDeleteRequest {

    @NotNull
    @JsonProperty("card_owner_id")
    private Long cardOwnerId;

    @NotBlank
    @JsonProperty("set_code")
    private String setCode;

    @NotBlank
    @JsonProperty("collector_number")
    private String collectorNumber;

    public Long getCardOwnerId() {
        return cardOwnerId;
    }

    public void setCardOwnerId(Long cardOwnerId) {
        this.cardOwnerId = cardOwnerId;
    }

    public String getSetCode() {
        return setCode;
    }

    public void setSetCode(String setCode) {
        this.setCode = setCode;
    }

    public String getCollectorNumber() {
        return collectorNumber;
    }

    public void setCollectorNumber(String collectorNumber) {
        this.collectorNumber = collectorNumber;
    }
}
