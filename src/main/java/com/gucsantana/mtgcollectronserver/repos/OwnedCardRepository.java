package com.gucsantana.mtgcollectronserver.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.gucsantana.mtgcollectronserver.models.OwnedCard;
import com.gucsantana.mtgcollectronserver.models.OwnedCard.OwnedCardId;

public interface OwnedCardRepository extends JpaRepository<OwnedCard,OwnedCardId>{
    Optional<OwnedCard> findByCardOwnerIdAndSetCodeAndCollectorNumber(Long userId, String setCode, String collectorNumber);
    List<OwnedCard> findByCardOwnerId(Long userId);
    List<OwnedCard> findByCardOwnerIdAndSetCode(Long userId,String setCode);
    List<OwnedCard> findByCardOwnerIdAndCardName(Long userId, String cardName);
    List<OwnedCard> findByCardOwnerIdAndSetCodeAndCardNameAndCollectorNumber(Long userId,String setCode, String cardName, String collectorNumber);
    List<OwnedCard> findByCardOwnerIdAndCardNameContainingIgnoreCase(Long userId, String cardName);
    @Transactional
    List<OwnedCard> deleteByCardOwnerIdAndSetCodeAndCollectorNumber(Long userId,String setCode, String collectorNumber);
    @Transactional
    void deleteByCardOwnerId(Long userId);
}
