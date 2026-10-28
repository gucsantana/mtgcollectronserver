package com.gucsantana.mtgcollectronserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserPreferencesPatchRequest {

    @JsonProperty("full_set_option_selected")
    private Integer fullSetOptionSelected;

    @JsonProperty("card_per_page_option_selected")
    private Integer cardPerPageOptionSelected;

    @JsonProperty("dark_mode")
    private Boolean darkMode;

    @JsonProperty("show_prices")
    private Boolean showPrices;

    @JsonProperty("show_sidebar_percent")
    private Boolean showSidebarPercent;

    public Integer getFullSetOptionSelected() {
        return fullSetOptionSelected;
    }

    public void setFullSetOptionSelected(Integer fullSetOptionSelected) {
        this.fullSetOptionSelected = fullSetOptionSelected;
    }

    public Integer getCardPerPageOptionSelected() {
        return cardPerPageOptionSelected;
    }

    public void setCardPerPageOptionSelected(Integer cardPerPageOptionSelected) {
        this.cardPerPageOptionSelected = cardPerPageOptionSelected;
    }

    public Boolean getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(Boolean darkMode) {
        this.darkMode = darkMode;
    }

    public Boolean getShowPrices() {
        return showPrices;
    }

    public void setShowPrices(Boolean showPrices) {
        this.showPrices = showPrices;
    }

    public Boolean getShowSidebarPercent() {
        return showSidebarPercent;
    }

    public void setShowSidebarPercent(Boolean showSidebarPercent) {
        this.showSidebarPercent = showSidebarPercent;
    }
}
