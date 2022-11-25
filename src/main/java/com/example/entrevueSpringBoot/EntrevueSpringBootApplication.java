package com.example.entrevueSpringBoot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class EntrevueSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(EntrevueSpringBootApplication.class, args);
	}
}
