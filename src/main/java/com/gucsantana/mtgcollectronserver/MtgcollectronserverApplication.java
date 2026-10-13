package com.gucsantana.mtgcollectronserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableCaching 
@OpenAPIDefinition(info = @Info(title="MTG Collectron Control API", version="0.1", description = "Handles database operations for the MTG Collectron site"))
public class MtgcollectronserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(MtgcollectronserverApplication.class, args);
	}

}
