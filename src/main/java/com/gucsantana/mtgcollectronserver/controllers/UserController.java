package com.gucsantana.mtgcollectronserver.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gucsantana.mtgcollectronserver.models.User;
import com.gucsantana.mtgcollectronserver.repos.UserRepository;

import java.util.List;

import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/user")
@Tag(name="User REST Endpoint")
public class UserController {

    @Autowired 
    UserRepository userRepo;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/")
    public List<User> getAllUsers(){
        return userRepo.findAll();
    }

    @GetMapping("/{id}")
    @Transactional
    @Cacheable (value = "user-cache")
    @Operation (summary = "Returns a single user by ID")
    public User getUser(@Parameter(description = "User ID") @PathVariable int id){
        LOGGER.info("Finding user by Id: "+id);
        return userRepo.findById(id).get();
    }
    
    @PostMapping("/")
    @Operation (summary = "Creates a user by passing in a pre-filled User instance")
    public User createUser(@Valid @RequestBody User user){
        LOGGER.info("Created user: "+ user.getUsername());
        return userRepo.save(user);
    }

    @PutMapping("/")
    @Operation (summary = "Updates a user by passing in a pre-filled User instance; uses ID to match")
    public User updateUser(@RequestBody User user){
        LOGGER.info("Updated user: "+ user.getUsername());
        return userRepo.save(user);
    }

    @DeleteMapping("/{id}")
    @CacheEvict (value = "user-cache")
    @Operation (summary = "Deletes a user by passing its ID")
    public void deleteUser(@PathVariable int id){
        userRepo.deleteById(id);
    }
}
