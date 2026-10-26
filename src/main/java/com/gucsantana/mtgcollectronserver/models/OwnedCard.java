package com.gucsantana.mtgcollectronserver.models;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "user_cards")
@IdClass(OwnedCard.OwnedCardId.class)
public class OwnedCard implements Serializable {

    @Id
    @NotNull
    @Column(name = "card_owner_id", nullable = false)
    @JsonProperty("card_owner_id")
    private Long cardOwnerId;

    @Id
    @NotNull
    @Column(name = "set_code", length = 15, nullable = false)
    @JsonProperty("set_code")
    private String setCode;

    @NotNull
    @Column(name = "card_name", length = 200, nullable = false)
    @JsonProperty("card_name")
    private String cardName;

    @Id
    @NotNull
    @Column(name = "collector_number", length = 20, nullable = false)
    @JsonProperty("collector_number")
    private String collectorNumber;

    @Column(name = "card_count")
    @JsonProperty("card_count")
    private Integer cardCount;

    @NotNull
    @Column(name = "is_foil", nullable = false)
    @JsonProperty("is_foil")
    private Boolean isFoil;

    @Column(name = "is_considered_extra")
    @JsonProperty("is_considered_extra")
    private Boolean isConsideredExtra;

    @Column(name = "is_tagged_square")
    @JsonProperty("is_tagged_square")
    private Boolean isTaggedSquare;

    @Column(name = "is_tagged_circle")
    @JsonProperty("is_tagged_circle")
    private Boolean isTaggedCircle;

    @Column(name = "is_tagged_triangle")
    @JsonProperty("is_tagged_triangle")
    private Boolean isTaggedTriangle;

    @Column(name = "is_tagged_cross")
    @JsonProperty("is_tagged_cross")
    private Boolean isTaggedCross;

    public static class OwnedCardId implements Serializable {
        private Long cardOwnerId;
        private String setCode;
        private String collectorNumber;

        public OwnedCardId() {
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof OwnedCardId that)) {
                return false;
            }
            return Objects.equals(cardOwnerId, that.cardOwnerId)
                    && Objects.equals(setCode, that.setCode)
                    && Objects.equals(collectorNumber, that.collectorNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cardOwnerId, setCode, collectorNumber);
        }
    }

    public Long getCardOwnerId() {
        return cardOwnerId;
    }

    public void setCardOwnerId(Long card_owner_id) {
        this.cardOwnerId = card_owner_id;
    }

    public String getSetCode() {
        return setCode;
    }

    public void setSetCode(String set_code) {
        this.setCode = set_code;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String card_name) {
        this.cardName = card_name;
    }

    public String getCollectorNumber() {
        return collectorNumber;
    }

    public void setCollectorNumber(String collector_number) {
        this.collectorNumber = collector_number;
    }

    public Integer getCardCount() {
        return cardCount;
    }

    public void setCardCount(Integer card_count) {
        this.cardCount = card_count;
    }

    public Boolean getIsFoil() {
        return isFoil;
    }

    public void setIsFoil(Boolean is_foil) {
        this.isFoil = is_foil;
    }

    public Boolean getIsConsideredExtra() {
        return isConsideredExtra;
    }

    public void setIsConsideredExtra(Boolean is_considered_extra) {
        this.isConsideredExtra = is_considered_extra;
    }

    public Boolean getIsTaggedSquare() {
        return isTaggedSquare;
    }

    public void setIsTaggedSquare(Boolean is_tagged_square) {
        this.isTaggedSquare = is_tagged_square;
    }

    public Boolean getIsTaggedCircle() {
        return isTaggedCircle;
    }

    public void setIsTaggedCircle(Boolean is_tagged_circle) {
        this.isTaggedCircle = is_tagged_circle;
    }

    public Boolean getIsTaggedTriangle() {
        return isTaggedTriangle;
    }

    public void setIsTaggedTriangle(Boolean is_tagged_triangle) {
        this.isTaggedTriangle = is_tagged_triangle;
    }

    public Boolean getIsTaggedCross() {
        return isTaggedCross;
    }

    public void setIsTaggedCross(Boolean is_tagged_cross) {
        this.isTaggedCross = is_tagged_cross;
    }
}
