package com.barefoot.security;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String[] passwords = {"admin123", "user123", "test123"};

        System.out.println("=================================================");
        System.out.println("  Barefoot - Generador de Contraseñas BCrypt");
        System.out.println("=================================================\n");

        for (String password : passwords) {
            String hashedPassword = encoder.encode(password);
            System.out.println("Password original: " + password);
            System.out.println("Password encriptado: " + hashedPassword);
            System.out.println("-------------------------------------------------\n");
        }


        String testPassword = "admin123";
        String testHash = encoder.encode(testPassword);
        boolean matches = encoder.matches(testPassword, testHash);

        System.out.println("Verificación:");
        System.out.println("¿La contraseña '" + testPassword + "' coincide? " + matches);
        System.out.println("\n=================================================");
    }


    public static String generatePassword(String plainPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(plainPassword);
    }


    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(plainPassword, hashedPassword);
    }
}