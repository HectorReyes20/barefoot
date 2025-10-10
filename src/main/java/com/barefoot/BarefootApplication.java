package com.barefoot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BarefootApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarefootApplication.class, args);
		System.out.println("\n=================================================");
		System.out.println("  Barefoot E-commerce Application Started!");
		System.out.println("  Access: http://localhost:8080/login");
		System.out.println("=================================================\n");
	}

}
