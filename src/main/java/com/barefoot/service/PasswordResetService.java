package com.barefoot.service;

import com.barefoot.model.Usuario;
import com.barefoot.repository.UsuarioRepository;
// Imports de Resend
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Inyectamos la clave desde application.properties
    @Value("${resend.api.key}")
    private String resendApiKey;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void solicitarRecuperacion(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setTokenExpiration(LocalDateTime.now().plusMinutes(15));
            usuarioRepository.save(usuario);

            // IMPORTANTE: Cambia esto por tu dominio de Railway cuando despliegues
            // Local: http://localhost:8080...
            // Prod: https://barefoot-production.up.railway.app...
            String baseUrl = "http://localhost:8080";
            String link = baseUrl + "/reset-password?token=" + token;

            enviarEmailResend(email, link, usuario.getNombre());
        }
    }

    private void enviarEmailResend(String emailDestino, String link, String nombre) {
        // 1. Inicializar Resend
        Resend resend = new Resend(resendApiKey);

        // 2. Crear HTML bonito
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; max-width: 500px; margin: 0 auto;">
                <h2 style="color: #4E342E; text-align: center; border-bottom: 2px solid #CCA474; padding-bottom: 10px;">Barefoot Store</h2>
                <p>Hola <strong>%s</strong>,</p>
                <p>Has solicitado restablecer tu contraseña. Haz clic en el siguiente botón para crear una nueva:</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #4E342E; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        Restablecer Contraseña
                    </a>
                </div>
                <p style="color: #888; font-size: 12px; text-align: center;">Si no fuiste tú, simplemente ignora este mensaje.</p>
            </div>
            """.formatted(nombre, link);

        // 3. Configurar el correo
        // OJO: Si usas la cuenta gratuita de Resend sin dominio propio,
        // el 'from' DEBE ser "onboarding@resend.dev"
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Barefoot Store <onboarding@resend.dev>")
                .to(emailDestino)
                .subject("Recupera tu acceso - Barefoot Store")
                .html(htmlContent)
                .build();

        try {
            resend.emails().send(params);
            System.out.println("✅ Correo enviado a: " + emailDestino);
        } catch (ResendException e) {
            e.printStackTrace();
            System.err.println("❌ Error enviando correo: " + e.getMessage());
        }
    }

    public Usuario obtenerUsuarioPorToken(String token) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetToken(token);
        if (usuarioOpt.isPresent() && usuarioOpt.get().getTokenExpiration().isAfter(LocalDateTime.now())) {
            return usuarioOpt.get();
        }
        return null;
    }

    public void cambiarPassword(Usuario usuario, String nuevaPassword) {
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setResetToken(null);
        usuario.setTokenExpiration(null);
        usuarioRepository.save(usuario);
    }
}