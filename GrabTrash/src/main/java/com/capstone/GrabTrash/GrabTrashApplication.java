package com.capstone.GrabTrash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.capstone.GrabTrash"})
@SpringBootApplication
public class GrabTrashApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrabTrashApplication.class, args);
	}

}
