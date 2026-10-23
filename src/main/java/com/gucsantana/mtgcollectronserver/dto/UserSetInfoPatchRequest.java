package com.gucsantana.mtgcollectronserver.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserSetInfoPatchRequest {

    @NotNull
    @JsonProperty("card_owned_id")
    @JsonAlias("card_owner_id")
    private Long cardOwnerId;

    @NotBlank
    @JsonProperty("set_code")
    private String setCode;

    private Long commonsCount;
    private Long uncommonsCount;
    private Long raresCount;
    private Long mythicsCount;
    private Long baseSetOwned;
    private Long extraSetOwned;
    private Long foilsOwned;
    private Float fullSetEverySingle;
    private Float fullSetBaseOnly;
    private Float fullSetOneEach;

    private boolean commonsCountProvided;
    private boolean uncommonsCountProvided;
    private boolean raresCountProvided;
    private boolean mythicsCountProvided;
    private boolean baseSetOwnedProvided;
    private boolean extraSetOwnedProvided;
    private boolean foilsOwnedProvided;
    private boolean fullSetEverySingleProvided;
    private boolean fullSetBaseOnlyProvided;
    private boolean fullSetOneEachProvided;

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

    @JsonProperty("commons_count")
    public void setCommonsCount(Long commonsCount) {
        this.commonsCountProvided = true;
        this.commonsCount = commonsCount;
    }

    public Long getCommonsCount() {
        return commonsCount;
    }

    public boolean hasCommonsCount() {
        return commonsCountProvided;
    }

    @JsonProperty("uncommons_count")
    public void setUncommonsCount(Long uncommonsCount) {
        this.uncommonsCountProvided = true;
        this.uncommonsCount = uncommonsCount;
    }

    public Long getUncommonsCount() {
        return uncommonsCount;
    }

    public boolean hasUncommonsCount() {
        return uncommonsCountProvided;
    }

    @JsonProperty("rares_count")
    public void setRaresCount(Long raresCount) {
        this.raresCountProvided = true;
        this.raresCount = raresCount;
    }

    public Long getRaresCount() {
        return raresCount;
    }

    public boolean hasRaresCount() {
        return raresCountProvided;
    }

    @JsonProperty("mythics_count")
    public void setMythicsCount(Long mythicsCount) {
        this.mythicsCountProvided = true;
        this.mythicsCount = mythicsCount;
    }

    public Long getMythicsCount() {
        return mythicsCount;
    }

    public boolean hasMythicsCount() {
        return mythicsCountProvided;
    }

    @JsonProperty("base_set_owned")
    public void setBaseSetOwned(Long baseSetOwned) {
        this.baseSetOwnedProvided = true;
        this.baseSetOwned = baseSetOwned;
    }

    public Long getBaseSetOwned() {
        return baseSetOwned;
    }

    public boolean hasBaseSetOwned() {
        return baseSetOwnedProvided;
    }

    @JsonProperty("extra_set_owned")
    public void setExtraSetOwned(Long extraSetOwned) {
        this.extraSetOwnedProvided = true;
        this.extraSetOwned = extraSetOwned;
    }

    public Long getExtraSetOwned() {
        return extraSetOwned;
    }

    public boolean hasExtraSetOwned() {
        return extraSetOwnedProvided;
    }

    @JsonProperty("foils_owned")
    public void setFoilsOwned(Long foilsOwned) {
        this.foilsOwnedProvided = true;
        this.foilsOwned = foilsOwned;
    }

    public Long getFoilsOwned() {
        return foilsOwned;
    }

    public boolean hasFoilsOwned() {
        return foilsOwnedProvided;
    }

    @JsonProperty("full_set_every_single")
    public void setFullSetEverySingle(Float fullSetEverySingle) {
        this.fullSetEverySingleProvided = true;
        this.fullSetEverySingle = fullSetEverySingle;
    }

    public Float getFullSetEverySingle() {
        return fullSetEverySingle;
    }

    public boolean hasFullSetEverySingle() {
        return fullSetEverySingleProvided;
    }

    @JsonProperty("full_set_base_only")
    public void setFullSetBaseOnly(Float fullSetBaseOnly) {
        this.fullSetBaseOnlyProvided = true;
        this.fullSetBaseOnly = fullSetBaseOnly;
    }

    public Float getFullSetBaseOnly() {
        return fullSetBaseOnly;
    }

    public boolean hasFullSetBaseOnly() {
        return fullSetBaseOnlyProvided;
    }

    @JsonProperty("full_set_one_each")
    public void setFullSetOneEach(Float fullSetOneEach) {
        this.fullSetOneEachProvided = true;
        this.fullSetOneEach = fullSetOneEach;
    }

    public Float getFullSetOneEach() {
        return fullSetOneEach;
    }

    public boolean hasFullSetOneEach() {
        return fullSetOneEachProvided;
    }
}
