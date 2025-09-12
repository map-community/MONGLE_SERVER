package com.algangi.mongle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MongleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MongleApplication.class, args);
	}

}
