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
@Table(name = "user_set_info")
@IdClass(UserSetInfo.UserSetInfoId.class)
public class UserSetInfo implements Serializable {

    @Id
    @NotNull
    @Column(name = "card_owner_id", nullable = false)
    @JsonProperty("card_owner_id")
    private Long cardOwnerId;

    @Id
    @NotNull
    @Column(name = "set_code", length = 10, nullable = false)
    @JsonProperty("set_code")
    private String setCode;

    @Column(name = "commons_count")
    @JsonProperty("commons_count")
    private Long commonsCount;

    @Column(name = "uncommons_count")
    @JsonProperty("uncommons_count")
    private Long uncommonsCount;

    @Column(name = "rares_count")
    @JsonProperty("rares_count")
    private Long raresCount;

    @Column(name = "mythics_count")
    @JsonProperty("mythics_count")
    private Long mythicsCount;

    @Column(name = "base_set_owned")
    @JsonProperty("base_set_owned")
    private Long baseSetOwned;

    @Column(name = "extra_set_owned")
    @JsonProperty("extra_set_owned")
    private Long extraSetOwned;

    @Column(name = "foils_owned")
    @JsonProperty("foils_owned")
    private Long foilsOwned;

    @Column(name = "full_set_every_single")
    @JsonProperty("full_set_every_single")
    private Float fullSetEverySingle;

    @Column(name = "full_set_base_only")
    @JsonProperty("full_set_base_only")
    private Float fullSetBaseOnly;

    @Column(name = "full_set_one_each")
    @JsonProperty("full_set_one_each")
    private Float fullSetOneEach;

    public static class UserSetInfoId implements Serializable {
        private Long cardOwnerId;
        private String setCode;

        public UserSetInfoId() {
        }

        public UserSetInfoId(Long cardOwnerId, String setCode) {
            this.cardOwnerId = cardOwnerId;
            this.setCode = setCode;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof UserSetInfoId that)) {
                return false;
            }
            return Objects.equals(cardOwnerId, that.cardOwnerId)
                    && Objects.equals(setCode, that.setCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cardOwnerId, setCode);
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

    public Long getCommonsCount() {
        return commonsCount;
    }

    public void setCommonsCount(Long commons_count) {
        this.commonsCount = commons_count;
    }

    public Long getUncommonsCount() {
        return uncommonsCount;
    }

    public void setUncommonsCount(Long uncommons_count) {
        this.uncommonsCount = uncommons_count;
    }

    public Long getRaresCount() {
        return raresCount;
    }

    public void setRaresCount(Long rares_count) {
        this.raresCount = rares_count;
    }

    public Long getMythicsCount() {
        return mythicsCount;
    }

    public void setMythicsCount(Long mythics_count) {
        this.mythicsCount = mythics_count;
    }

    public Long getBaseSetOwned() {
        return baseSetOwned;
    }

    public void setBaseSetOwned(Long base_set_owned) {
        this.baseSetOwned = base_set_owned;
    }

    public Long getExtraSetOwned() {
        return extraSetOwned;
    }

    public void setExtraSetOwned(Long extra_set_owned) {
        this.extraSetOwned = extra_set_owned;
    }

    public Long getFoilsOwned() {
        return foilsOwned;
    }

    public void setFoilsOwned(Long foils_owned) {
        this.foilsOwned = foils_owned;
    }

    public Float getFullSetEverySingle() {
        return fullSetEverySingle;
    }

    public void setFullSetEverySingle(Float full_set_every_single) {
        this.fullSetEverySingle = full_set_every_single;
    }

    public Float getFullSetBaseOnly() {
        return fullSetBaseOnly;
    }

    public void setFullSetBaseOnly(Float full_set_base_only) {
        this.fullSetBaseOnly = full_set_base_only;
    }

    public Float getFullSetOneEach() {
        return fullSetOneEach;
    }

    public void setFullSetOneEach(Float full_set_one_each) {
        this.fullSetOneEach = full_set_one_each;
    }
}
