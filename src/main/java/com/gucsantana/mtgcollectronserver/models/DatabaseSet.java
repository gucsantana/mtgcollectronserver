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
    @Column(name = "set_code", length = 10, nullable = false)
    private String set_code;

    @NotNull
    @Column(name = "set_name", length = 100, nullable = false)
    private String set_name;

    @NotNull
    @Column(name = "released_at", nullable = false)
    private LocalDateTime released_at;

    @NotNull
    @Column(name = "set_type", length = 20, nullable = false)
    private String set_type;

    @NotNull
    @Column(name = "card_count", nullable = false)
    private Integer card_count;

    @NotNull
    @Column(name = "digital", nullable = false)
    private Boolean digital;

    @NotNull
    @Column(name = "nonfoil_only", nullable = false)
    private Boolean nonfoil_only;

    @NotNull
    @Column(name = "foil_only", nullable = false)
    private Boolean foil_only;

    @Column(name = "icon_svg_uri", length = 100)
    private String icon_svg_uri;

    public String getSet_code() {
        return set_code;
    }

    public void setSet_code(String set_code) {
        this.set_code = set_code;
    }

    public String getSet_name() {
        return set_name;
    }

    public void setSet_name(String set_name) {
        this.set_name = set_name;
    }

    public LocalDateTime getReleased_at() {
        return released_at;
    }

    public void setReleased_at(LocalDateTime released_at) {
        this.released_at = released_at;
    }

    public String getSet_type() {
        return set_type;
    }

    public void setSet_type(String set_type) {
        this.set_type = set_type;
    }

    public Integer getCard_count() {
        return card_count;
    }

    public void setCard_count(Integer card_count) {
        this.card_count = card_count;
    }

    public Boolean getDigital() {
        return digital;
    }

    public void setDigital(Boolean digital) {
        this.digital = digital;
    }

    public Boolean getNonfoil_only() {
        return nonfoil_only;
    }

    public void setNonfoil_only(Boolean nonfoil_only) {
        this.nonfoil_only = nonfoil_only;
    }

    public Boolean getFoil_only() {
        return foil_only;
    }

    public void setFoil_only(Boolean foil_only) {
        this.foil_only = foil_only;
    }

    public String getIcon_svg_uri() {
        return icon_svg_uri;
    }

    public void setIcon_svg_uri(String icon_svg_uri) {
        this.icon_svg_uri = icon_svg_uri;
    }
}
