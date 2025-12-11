package com.barefoot.service;

import com.barefoot.model.Usuario;
import com.barefoot.repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JavaMailSender mailSender; // La herramienta de envío real

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
            usuario.setTokenExpiration(LocalDateTime.now().plusMinutes(15));
            usuarioRepository.save(usuario);

            // SIMULACIÓN DE ENVÍO DE CORREO
            String link = "http://localhost:8080/reset-password?token=" + token;

            // Enviar el correo
            try {
                enviarEmailRecuperacion(email, link, usuario.getNombre());
            } catch (MessagingException e) {
                e.printStackTrace(); // En producción usaríamos un Logger
            }
        }
        // Si el email no existe, no hacemos nada por seguridad (para no revelar qué correos existen)
    }

    private void enviarEmailRecuperacion(String email, String link, String nombre) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("TU_CORREO_REAL@gmail.com"); // ¡PON TU MISMO CORREO AQUÍ!
        helper.setTo(email);
        helper.setSubject("Recupera tu contraseña - Barefoot Store");

        // Diseño del correo en HTML
        String contenidoHtml = """
            <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; max-width: 600px; margin: auto;">
                <h2 style="color: #4E342E; text-align: center;">Barefoot Store</h2>
                <hr>
                <p>Hola <strong>%s</strong>,</p>
                <p>Recibimos una solicitud para restablecer tu contraseña. Si fuiste tú, haz clic en el botón de abajo:</p>
                
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #C0A080; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                        Restablecer Contraseña
                    </a>
                </div>
                
                <p style="font-size: 12px; color: #777;">Si no solicitaste este cambio, ignora este correo. Tu cuenta sigue segura.</p>
                <hr>
                <p style="text-align: center; font-size: 10px; color: #999;">&copy; 2025 Barefoot Store</p>
            </div>
            """.formatted(nombre, link);

        helper.setText(contenidoHtml, true); // 'true' habilita HTML

        mailSender.send(message);
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
        usuario.setResetToken(null);
        usuario.setTokenExpiration(null);
        usuarioRepository.save(usuario);
    }
}