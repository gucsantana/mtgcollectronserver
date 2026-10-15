package com.gucsantana.mtgcollectronserver.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity 
@Table(name = "cards")
public class DatabaseCard implements Serializable {

    @Id
    @NotNull 
    private String id;

    @NotNull 
    @Column(name="set_code")
    @JsonProperty("set_code")
    private String setCode;
    @NotNull 
    private String name;
    @NotNull 
    @Column(name="collector_number")
    @JsonProperty("collector_number")
    private String collectorNumber;
    @NotNull 
    private String rarity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> prices;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="image_uris", columnDefinition = "json")
    @JsonProperty("image_uris")
    private Map<String, String> imageUris;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="card_faces", columnDefinition = "json")
    @JsonProperty("card_faces")
    private List<Map<String, Object>> cardFaces;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="promo_types", columnDefinition = "json")
    @JsonProperty("promo_types")
    private List<String> promoTypes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<String> finishes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSetCode() {
        return setCode;
    }

    public void setSetCode(String set_code) {
        this.setCode = set_code;
    }

    public String getName() {
        return name;
    }

    public void setName(String card_name) {
        this.name = card_name;
    }

    public String getCollectorNumber() {
        return collectorNumber;
    }

    public void setCollectorNumber(String collector_number) {
        this.collectorNumber = collector_number;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public Map<String, Object> getPrices() {
        return prices;
    }

    public void setPrices(Map<String, Object> prices) {
        this.prices = prices;
    }

    public Map<String, String> getImageUris() {
        return imageUris;
    }

    public void setImageUris(Map<String, String> image_uris) {
        this.imageUris = image_uris;
    }

    public List<Map<String, Object>> getCardFaces() {
        return cardFaces;
    }

    public void setCardFaces(List<Map<String, Object>> card_faces) {
        this.cardFaces = card_faces;
    }

    public List<String> getPromoTypes() {
        return promoTypes;
    }

    public void setPromoTypes(List<String> promo_types) {
        this.promoTypes = promo_types;
    }

    public List<String> getFinishes() {
        return finishes;
    }

    public void setFinishes(List<String> finishes) {
        this.finishes = finishes;
    }
}
