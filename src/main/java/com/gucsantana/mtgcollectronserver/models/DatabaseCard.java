package com.gucsantana.mtgcollectronserver.models;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity 
@Table(name = "cards")
public class DatabaseCard implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull 
    @Column(name="set_code")
    private String setCode;
    @NotNull 
    @Column(name="card_name")
    private String cardName;
    @NotNull 
    @Column(name="collector_number")
    private String collectorNumber;
    @NotNull 
    private String rarity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String prices;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="image_uris", columnDefinition = "json")
    private String imageUris;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="promo_types", columnDefinition = "json")
    private String promoTypes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getPrices() {
        return prices;
    }

    public void setPrices(String prices) {
        this.prices = prices;
    }

    public String getImageUris() {
        return imageUris;
    }

    public void setImageUris(String image_uris) {
        this.imageUris = image_uris;
    }

    public String getPromoTypes() {
        return promoTypes;
    }

    public void setPromoTypes(String promo_types) {
        this.promoTypes = promo_types;
    }
}
