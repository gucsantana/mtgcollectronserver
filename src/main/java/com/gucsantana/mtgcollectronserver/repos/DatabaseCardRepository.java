package com.gucsantana.mtgcollectronserver.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gucsantana.mtgcollectronserver.models.DatabaseCard;

import java.util.List;


public interface DatabaseCardRepository extends JpaRepository<DatabaseCard,String>{
    List<DatabaseCard> findBySetCode(String set);
    List<DatabaseCard> findBySetCodeAndName(String set, String name);
    List<DatabaseCard> findBySetCodeAndNameAndCollectorNumber(String set, String name, String collector_number);
}
