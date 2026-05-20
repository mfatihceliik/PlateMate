package com.mefy.platemate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlatemateApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlatemateApplication.class, args);
	}

}
