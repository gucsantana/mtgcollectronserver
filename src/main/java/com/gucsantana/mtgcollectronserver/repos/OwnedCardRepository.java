package com.gucsantana.mtgcollectronserver.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gucsantana.mtgcollectronserver.models.OwnedCard;
import com.gucsantana.mtgcollectronserver.models.OwnedCard.OwnedCardId;

public interface OwnedCardRepository extends JpaRepository<OwnedCard,OwnedCardId>{

}
