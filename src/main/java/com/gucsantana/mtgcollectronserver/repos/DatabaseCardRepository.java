package com.gucsantana.mtgcollectronserver.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gucsantana.mtgcollectronserver.models.DatabaseCard;

import java.util.List;


public interface DatabaseCardRepository extends JpaRepository<DatabaseCard,Integer>{
    List<DatabaseCard> findBySetCode(String setCode);
    List<DatabaseCard> findBySetCodeAndCardName(String setCode, String cardName);
    List<DatabaseCard> findBySetCodeAndCardNameAndCollectorNumber(String setCode, String cardName, String collector_number);
}
