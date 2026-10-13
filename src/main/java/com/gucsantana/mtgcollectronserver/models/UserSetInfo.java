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
@Table(name = "user_set_info")
@IdClass(UserSetInfo.UserSetInfoId.class)
public class UserSetInfo implements Serializable {

    @Id
    @NotNull
    @Column(name = "card_owner_id", nullable = false)
    private Long card_owner_id;

    @Id
    @NotNull
    @Column(name = "set_code", length = 10, nullable = false)
    private String set_code;

    @Column(name = "commons_count")
    private Long commons_count;

    @Column(name = "uncommons_count")
    private Long uncommons_count;

    @Column(name = "rares_count")
    private Long rares_count;

    @Column(name = "mythics_count")
    private Long mythics_count;

    @Column(name = "base_set_owned")
    private Long base_set_owned;

    @Column(name = "extra_set_owned")
    private Long extra_set_owned;

    @Column(name = "foils_owned")
    private Long foils_owned;

    @Column(name = "full_set_every_single")
    private Float full_set_every_single;

    @Column(name = "full_set_base_only")
    private Float full_set_base_only;

    @Column(name = "full_set_one_each")
    private Float full_set_one_each;

    public static class UserSetInfoId implements Serializable {
        private Long card_owner_id;
        private String set_code;

        public UserSetInfoId() {
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof UserSetInfoId that)) {
                return false;
            }
            return Objects.equals(card_owner_id, that.card_owner_id)
                    && Objects.equals(set_code, that.set_code);
        }

        @Override
        public int hashCode() {
            return Objects.hash(card_owner_id, set_code);
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

    public Long getCommons_count() {
        return commons_count;
    }

    public void setCommons_count(Long commons_count) {
        this.commons_count = commons_count;
    }

    public Long getUncommons_count() {
        return uncommons_count;
    }

    public void setUncommons_count(Long uncommons_count) {
        this.uncommons_count = uncommons_count;
    }

    public Long getRares_count() {
        return rares_count;
    }

    public void setRares_count(Long rares_count) {
        this.rares_count = rares_count;
    }

    public Long getMythics_count() {
        return mythics_count;
    }

    public void setMythics_count(Long mythics_count) {
        this.mythics_count = mythics_count;
    }

    public Long getBase_set_owned() {
        return base_set_owned;
    }

    public void setBase_set_owned(Long base_set_owned) {
        this.base_set_owned = base_set_owned;
    }

    public Long getExtra_set_owned() {
        return extra_set_owned;
    }

    public void setExtra_set_owned(Long extra_set_owned) {
        this.extra_set_owned = extra_set_owned;
    }

    public Long getFoils_owned() {
        return foils_owned;
    }

    public void setFoils_owned(Long foils_owned) {
        this.foils_owned = foils_owned;
    }

    public Float getFull_set_every_single() {
        return full_set_every_single;
    }

    public void setFull_set_every_single(Float full_set_every_single) {
        this.full_set_every_single = full_set_every_single;
    }

    public Float getFull_set_base_only() {
        return full_set_base_only;
    }

    public void setFull_set_base_only(Float full_set_base_only) {
        this.full_set_base_only = full_set_base_only;
    }

    public Float getFull_set_one_each() {
        return full_set_one_each;
    }

    public void setFull_set_one_each(Float full_set_one_each) {
        this.full_set_one_each = full_set_one_each;
    }
}
