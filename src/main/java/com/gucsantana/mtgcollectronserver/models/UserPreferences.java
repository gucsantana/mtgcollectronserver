package com.gucsantana.mtgcollectronserver.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "user_preferences")
public class UserPreferences implements Serializable {

    @Id
    @NotNull
    @Column(name = "user_id", nullable = false)
    @JsonProperty("user_id")
    private Long userId;

    @NotNull
    @Column(name = "full_set_option_selected", nullable = false)
    @JsonProperty("full_set_option_selected")
    private Integer fullSetOptionSelected;

    @NotNull
    @Column(name = "card_per_page_option_selected", nullable = false)
    @JsonProperty("card_per_page_option_selected")
    private Integer cardPerPageOptionSelected;

    @Column(name = "dark_mode")
    @JsonProperty("dark_mode")
    private Boolean darkMode;

    @Column(name = "show_prices")
    @JsonProperty("show_prices")
    private Boolean showPrices;

    @Column(name = "show_sidebar_percent")
    @JsonProperty("show_sidebar_percent")
    private Boolean showSidebarPercent;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long user_id) {
        this.userId = user_id;
    }

    public Integer getFullSetOptionSelected() {
        return fullSetOptionSelected;
    }

    public void setFullSetOptionSelected(Integer full_set_option_selected) {
        this.fullSetOptionSelected = full_set_option_selected;
    }

    public Integer getCardPerPageOptionSelected() {
        return cardPerPageOptionSelected;
    }

    public void setCardPerPageOptionSelected(Integer card_per_page_option_selected) {
        this.cardPerPageOptionSelected = card_per_page_option_selected;
    }

    public Boolean getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(Boolean dark_mode) {
        this.darkMode = dark_mode;
    }

    public Boolean getShowPrices() {
        return showPrices;
    }

    public void setShowPrices(Boolean show_prices) {
        this.showPrices = show_prices;
    }

    public Boolean getShowSidebarPercent() {
        return showSidebarPercent;
    }

    public void setShowSidebarPercent(Boolean show_sidebar_percent) {
        this.showSidebarPercent = show_sidebar_percent;
    }
}
