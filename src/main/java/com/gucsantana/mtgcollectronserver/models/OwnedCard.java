package com.gucsantana.mtgcollectronserver.models;

import java.io.Serializable;
import java.util.Objects;

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
    private Long card_owner_id;

    @Id
    @NotNull
    @Column(name = "set_code", length = 10, nullable = false)
    private String set_code;

    @Id
    @NotNull
    @Column(name = "collector_number", length = 7, nullable = false)
    private String collector_number;

    @Column(name = "card_count")
    private Integer card_count;

    @NotNull
    @Column(name = "is_foil", nullable = false)
    private Boolean is_foil;

    @Column(name = "is_considered_extra")
    private Boolean is_considered_extra;

    @Column(name = "is_tagged_square")
    private Boolean is_tagged_square;

    @Column(name = "is_tagged_circle")
    private Boolean is_tagged_circle;

    @Column(name = "is_tagged_triangle")
    private Boolean is_tagged_triangle;

    @Column(name = "is_tagged_cross")
    private Boolean is_tagged_cross;

    public static class OwnedCardId implements Serializable {
        private Long card_owner_id;
        private String set_code;
        private String collector_number;

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
            return Objects.equals(card_owner_id, that.card_owner_id)
                    && Objects.equals(set_code, that.set_code)
                    && Objects.equals(collector_number, that.collector_number);
        }

        @Override
        public int hashCode() {
            return Objects.hash(card_owner_id, set_code, collector_number);
        }
    }

    public Long getCard_owner_id() {
        return card_owner_id;
    }

    public void setCard_owner_id(Long card_owner_id) {
        this.card_owner_id = card_owner_id;
    }

    public String getSet_code() {
        return set_code;
    }

    public void setSet_code(String set_code) {
        this.set_code = set_code;
    }

    public String getCollector_number() {
        return collector_number;
    }

    public void setCollector_number(String collector_number) {
        this.collector_number = collector_number;
    }

    public Integer getCard_count() {
        return card_count;
    }

    public void setCard_count(Integer card_count) {
        this.card_count = card_count;
    }

    public Boolean getIs_foil() {
        return is_foil;
    }

    public void setIs_foil(Boolean is_foil) {
        this.is_foil = is_foil;
    }

    public Boolean getIs_considered_extra() {
        return is_considered_extra;
    }

    public void setIs_considered_extra(Boolean is_considered_extra) {
        this.is_considered_extra = is_considered_extra;
    }

    public Boolean getIs_tagged_square() {
        return is_tagged_square;
    }

    public void setIs_tagged_square(Boolean is_tagged_square) {
        this.is_tagged_square = is_tagged_square;
    }

    public Boolean getIs_tagged_circle() {
        return is_tagged_circle;
    }

    public void setIs_tagged_circle(Boolean is_tagged_circle) {
        this.is_tagged_circle = is_tagged_circle;
    }

    public Boolean getIs_tagged_triangle() {
        return is_tagged_triangle;
    }

    public void setIs_tagged_triangle(Boolean is_tagged_triangle) {
        this.is_tagged_triangle = is_tagged_triangle;
    }

    public Boolean getIs_tagged_cross() {
        return is_tagged_cross;
    }

    public void setIs_tagged_cross(Boolean is_tagged_cross) {
        this.is_tagged_cross = is_tagged_cross;
    }
}
