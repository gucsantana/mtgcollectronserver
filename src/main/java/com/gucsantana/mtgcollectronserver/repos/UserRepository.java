package com.gucsantana.mtgcollectronserver.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gucsantana.mtgcollectronserver.models.User;

public interface UserRepository extends JpaRepository<User,Long>{

}
