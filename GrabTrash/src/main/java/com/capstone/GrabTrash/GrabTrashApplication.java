package com.capstone.GrabTrash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GrabTrashApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrabTrashApplication.class, args);
	}

}
