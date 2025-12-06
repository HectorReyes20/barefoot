package com.barefoot.service;

import com.barefoot.model.Usuario;
import com.barefoot.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 1. Generar token y "enviar" correo
    public void solicitarRecuperacion(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // Generar token único
            String token = UUID.randomUUID().toString();

            // Guardar token y expiración (24 horas)
            usuario.setResetToken(token);
            usuario.setTokenExpiration(LocalDateTime.now().plusHours(24));
            usuarioRepository.save(usuario);

            // SIMULACIÓN DE ENVÍO DE CORREO
            String link = "http://localhost:8080/reset-password?token=" + token;

            System.out.println("========================================");
            System.out.println("CORREO SIMULADO PARA: " + email);
            System.out.println("Hola " + usuario.getNombre() + ",");
            System.out.println("Para restablecer tu contraseña, haz clic aquí:");
            System.out.println(link);
            System.out.println("========================================");
        }
        // Si el email no existe, no hacemos nada por seguridad (para no revelar qué correos existen)
    }

    // 2. Validar si el token es válido
    public Usuario obtenerUsuarioPorToken(String token) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetToken(token);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Verificar si no ha expirado
            if (usuario.getTokenExpiration().isAfter(LocalDateTime.now())) {
                return usuario;
            }
        }
        return null; // Token inválido o expirado
    }

    // 3. Actualizar la contraseña
    public void cambiarPassword(Usuario usuario, String nuevaPassword) {
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setResetToken(null); // Borrar el token usado
        usuario.setTokenExpiration(null);
        usuarioRepository.save(usuario);
    }
}