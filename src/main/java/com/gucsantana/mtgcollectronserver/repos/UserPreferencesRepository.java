package com.gucsantana.mtgcollectronserver.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gucsantana.mtgcollectronserver.models.UserPreferences;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences,Long>{

}
