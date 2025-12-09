package com.barefoot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration; // Importante para el exclude
import org.springframework.context.annotation.Bean; // <--- Nuevo
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // <--- Nuevo
import org.springframework.security.crypto.password.PasswordEncoder; // <--- Nuevo

// Mantenemos el exclude para evitar el login por defecto, pero permitimos usar herramientas de seguridad
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class BarefootApplication {

    public static void main(String[] args) {
        SpringApplication.run(BarefootApplication.class, args);
        System.out.println("\n=================================================");
        System.out.println("  Barefoot E-commerce Aplicación Iniciada!");
        System.out.println("  Acceso: http://localhost:8080/inicio");
        System.out.println("=================================================\n");
    }

    // --- AGREGAR ESTO AL FINAL ---
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // -----------------------------

}