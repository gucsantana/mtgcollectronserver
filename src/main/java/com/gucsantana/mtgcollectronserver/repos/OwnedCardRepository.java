package com.gucsantana.mtgcollectronserver.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gucsantana.mtgcollectronserver.models.OwnedCard;
import com.gucsantana.mtgcollectronserver.models.OwnedCard.OwnedCardId;

public interface OwnedCardRepository extends JpaRepository<OwnedCard,OwnedCardId>{
    List<OwnedCard> findByCardOwnerId(Long userId);
    List<OwnedCard> findByCardOwnerIdAndSetCode(Long userId,String setCode);
    List<OwnedCard> findByCardOwnerIdAndSetCodeAndCardName(Long userId,String setCode, String cardName);
    List<OwnedCard> findByCardOwnerIdAndSetCodeAndCardNameAndCollectorNumber(Long userId,String setCode, String cardName, String collectorNumber);
    List<OwnedCard> deleteByCardOwnerIdAndSetCodeAndCardNameAndCollectorNumber(Long userId,String setCode, String cardName, String collectorNumber);
}
