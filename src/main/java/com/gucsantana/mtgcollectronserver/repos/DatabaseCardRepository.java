package com.gucsantana.mtgcollectronserver.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gucsantana.mtgcollectronserver.models.DatabaseCard;

import java.util.Collection;
import java.util.List;


public interface DatabaseCardRepository extends JpaRepository<DatabaseCard,String>{
    List<DatabaseCard> findBySetCode(String set);
    List<DatabaseCard> findBySetCodeAndName(String set, String name);
    List<DatabaseCard> findBySetCodeAndCollectorNumber(String set, String collector_number);
    List<DatabaseCard> findBySetCodeIn(Collection<String> setCodes);
}
