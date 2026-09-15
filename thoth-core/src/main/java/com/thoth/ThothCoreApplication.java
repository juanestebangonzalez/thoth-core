package com.thoth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class ThothCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThothCoreApplication.class, args);
	}

}
