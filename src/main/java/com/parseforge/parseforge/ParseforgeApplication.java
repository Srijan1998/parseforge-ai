package com.parseforge.parseforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ParseforgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParseforgeApplication.class, args);
	}

}
