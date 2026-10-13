package com.gucsantana.mtgcollectronserver.models;

import java.io.Serializable;

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
    private Long user_id;

    @NotNull
    @Column(name = "full_set_option_selected", nullable = false)
    private Integer full_set_option_selected;

    @NotNull
    @Column(name = "card_per_page_option_selected", nullable = false)
    private Integer card_per_page_option_selected;

    @Column(name = "dark_mode")
    private Boolean dark_mode;

    @Column(name = "show_prices")
    private Boolean show_prices;

    @Column(name = "show_sidebar_percent")
    private Boolean show_sidebar_percent;

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Integer getFull_set_option_selected() {
        return full_set_option_selected;
    }

    public void setFull_set_option_selected(Integer full_set_option_selected) {
        this.full_set_option_selected = full_set_option_selected;
    }

    public Integer getCard_per_page_option_selected() {
        return card_per_page_option_selected;
    }

    public void setCard_per_page_option_selected(Integer card_per_page_option_selected) {
        this.card_per_page_option_selected = card_per_page_option_selected;
    }

    public Boolean getDark_mode() {
        return dark_mode;
    }

    public void setDark_mode(Boolean dark_mode) {
        this.dark_mode = dark_mode;
    }

    public Boolean getShow_prices() {
        return show_prices;
    }

    public void setShow_prices(Boolean show_prices) {
        this.show_prices = show_prices;
    }

    public Boolean getShow_sidebar_percent() {
        return show_sidebar_percent;
    }

    public void setShow_sidebar_percent(Boolean show_sidebar_percent) {
        this.show_sidebar_percent = show_sidebar_percent;
    }
}
