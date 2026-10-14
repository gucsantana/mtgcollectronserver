package com.gucsantana.mtgcollectronserver.models;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "sets")
public class DatabaseSet implements Serializable {

    @Id
    @NotNull
    @Column(name = "code", length = 10, nullable = false)
    private String code;

    @NotNull
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @NotNull
    @Column(name = "released_at", nullable = false)
    private LocalDateTime releasedAt;

    @NotNull
    @Column(name = "set_type", length = 20, nullable = false)
    private String setType;

    @NotNull
    @Column(name = "card_count", nullable = false)
    private Integer cardCount;

    @NotNull
    @Column(name = "digital", nullable = false)
    private Boolean digital;

    @NotNull
    @Column(name = "nonfoil_only", nullable = false)
    private Boolean nonfoilOnly;

    @NotNull
    @Column(name = "foil_only", nullable = false)
    private Boolean foilOnly;

    @Column(name = "icon_svg_uri", length = 100)
    private String iconSvgUri;

    public String getCode() {
        return code;
    }

    public void setCode(String set_code) {
        this.code = set_code;
    }

    public String getName() {
        return name;
    }

    public void setName(String set_name) {
        this.name = set_name;
    }

    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(LocalDateTime released_at) {
        this.releasedAt = released_at;
    }

    public String getSetType() {
        return setType;
    }

    public void setSetType(String set_type) {
        this.setType = set_type;
    }

    public Integer getCardCount() {
        return cardCount;
    }

    public void setCardCount(Integer card_count) {
        this.cardCount = card_count;
    }

    public Boolean getDigital() {
        return digital;
    }

    public void setDigital(Boolean digital) {
        this.digital = digital;
    }

    public Boolean getNonfoilOnly() {
        return nonfoilOnly;
    }

    public void setNonfoilOnly(Boolean nonfoil_only) {
        this.nonfoilOnly = nonfoil_only;
    }

    public Boolean getFoilOnly() {
        return foilOnly;
    }

    public void setFoilOnly(Boolean foil_only) {
        this.foilOnly = foil_only;
    }

    public String getIconSvgUri() {
        return iconSvgUri;
    }

    public void setIconSvgUri(String icon_svg_uri) {
        this.iconSvgUri = icon_svg_uri;
    }
}
