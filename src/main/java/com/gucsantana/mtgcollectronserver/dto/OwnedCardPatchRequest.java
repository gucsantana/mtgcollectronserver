package com.gucsantana.mtgcollectronserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OwnedCardPatchRequest {

    @NotNull
    @JsonProperty("card_owner_id")
    private Long cardOwnerId;

    @NotBlank
    @JsonProperty("set_code")
    private String setCode;

    @NotBlank
    @JsonProperty("collector_number")
    private String collectorNumber;

    private String card_name;
    private Integer card_count;
    private Boolean is_foil;
    private Boolean is_considered_extra;
    private Boolean is_tagged_square;
    private Boolean is_tagged_circle;
    private Boolean is_tagged_triangle;
    private Boolean is_tagged_cross;

    private boolean cardNameProvided;
    private boolean cardCountProvided;
    private boolean isFoilProvided;
    private boolean isConsideredExtraProvided;
    private boolean isTaggedSquareProvided;
    private boolean isTaggedCircleProvided;
    private boolean isTaggedTriangleProvided;
    private boolean isTaggedCrossProvided;

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

    @JsonProperty("card_name")
    public void setCardName(String cardName) {
        this.cardNameProvided = true;
        this.card_name = cardName;
    }

    public String getCardName() {
        return card_name;
    }

    public boolean hasCardName() {
        return cardNameProvided;
    }

    @JsonProperty("card_count")
    public void setCardCount(Integer cardCount) {
        this.cardCountProvided = true;
        this.card_count = cardCount;
    }

    public Integer getCardCount() {
        return card_count;
    }

    public boolean hasCardCount() {
        return cardCountProvided;
    }

    @JsonProperty("is_foil")
    public void setIsFoil(Boolean isFoil) {
        this.isFoilProvided = true;
        this.is_foil = isFoil;
    }

    public Boolean getIsFoil() {
        return is_foil;
    }

    public boolean hasIsFoil() {
        return isFoilProvided;
    }

    @JsonProperty("is_considered_extra")
    public void setIsConsideredExtra(Boolean isConsideredExtra) {
        this.isConsideredExtraProvided = true;
        this.is_considered_extra = isConsideredExtra;
    }

    public Boolean getIsConsideredExtra() {
        return is_considered_extra;
    }

    public boolean hasIsConsideredExtra() {
        return isConsideredExtraProvided;
    }

    @JsonProperty("is_tagged_square")
    public void setIsTaggedSquare(Boolean isTaggedSquare) {
        this.isTaggedSquareProvided = true;
        this.is_tagged_square = isTaggedSquare;
    }

    public Boolean getIsTaggedSquare() {
        return is_tagged_square;
    }

    public boolean hasIsTaggedSquare() {
        return isTaggedSquareProvided;
    }

    @JsonProperty("is_tagged_circle")
    public void setIsTaggedCircle(Boolean isTaggedCircle) {
        this.isTaggedCircleProvided = true;
        this.is_tagged_circle = isTaggedCircle;
    }

    public Boolean getIsTaggedCircle() {
        return is_tagged_circle;
    }

    public boolean hasIsTaggedCircle() {
        return isTaggedCircleProvided;
    }

    @JsonProperty("is_tagged_triangle")
    public void setIsTaggedTriangle(Boolean isTaggedTriangle) {
        this.isTaggedTriangleProvided = true;
        this.is_tagged_triangle = isTaggedTriangle;
    }

    public Boolean getIsTaggedTriangle() {
        return is_tagged_triangle;
    }

    public boolean hasIsTaggedTriangle() {
        return isTaggedTriangleProvided;
    }

    @JsonProperty("is_tagged_cross")
    public void setIsTaggedCross(Boolean isTaggedCross) {
        this.isTaggedCrossProvided = true;
        this.is_tagged_cross = isTaggedCross;
    }

    public Boolean getIsTaggedCross() {
        return is_tagged_cross;
    }

    public boolean hasIsTaggedCross() {
        return isTaggedCrossProvided;
    }
}
