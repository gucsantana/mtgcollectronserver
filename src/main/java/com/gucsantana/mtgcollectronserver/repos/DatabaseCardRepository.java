package com.gucsantana.mtgcollectronserver.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gucsantana.mtgcollectronserver.models.DatabaseCard;

import java.util.List;


public interface DatabaseCardRepository extends JpaRepository<DatabaseCard,String>{
    List<DatabaseCard> findBySetCodeOrderByCollectorNumberAsc(String set);
    List<DatabaseCard> findBySetCodeAndNameOrderByCollectorNumberAsc(String set, String name);
    List<DatabaseCard> findBySetCodeAndNameAndCollectorNumberOrderByCollectorNumberAsc(String set, String name, String collector_number);
}
