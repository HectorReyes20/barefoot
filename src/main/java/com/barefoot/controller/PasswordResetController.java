package com.barefoot.controller;

import com.barefoot.model.Usuario;
import com.barefoot.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    @Autowired
    private PasswordResetService resetService;

    // 1. Mostrar pantalla "Olvide mi contraseña"
    @GetMapping("/forgot-password")
    public String mostrarFormularioOlvido() {
        return "auth/forgot-password";
    }

    // 2. Procesar el email ingresado
    @PostMapping("/forgot-password")
    public String procesarOlvido(@RequestParam String email, RedirectAttributes attributes) {
        resetService.solicitarRecuperacion(email);
        // Mensaje genérico por seguridad
        attributes.addFlashAttribute("mensaje", "Si el correo existe, recibirás un enlace para recuperar tu cuenta.");
        attributes.addFlashAttribute("tipoMensaje", "info");
        return "redirect:/login";
    }

    // 3. Mostrar pantalla de "Cambiar contraseña" (viene del link del correo)
    @GetMapping("/reset-password")
    public String mostrarFormularioCambio(@RequestParam String token, Model model) {
        Usuario usuario = resetService.obtenerUsuarioPorToken(token);

        if (usuario == null) {
            model.addAttribute("error", "El enlace es inválido o ha expirado.");
            return "auth/error-token"; // Crear una vista simple de error o redirigir a login
        }

        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    // 4. Procesar el cambio de contraseña final
    @PostMapping("/reset-password")
    public String procesarCambio(@RequestParam String token,
                                 @RequestParam String password,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes attributes) {

        Usuario usuario = resetService.obtenerUsuarioPorToken(token);

        if (usuario == null) {
            attributes.addFlashAttribute("error", "Token inválido.");
            return "redirect:/login";
        }

        if (!password.equals(confirmPassword)) {
            attributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/reset-password?token=" + token;
        }

        resetService.cambiarPassword(usuario, password);

        attributes.addFlashAttribute("mensaje", "Contraseña actualizada correctamente. Inicia sesión.");
        attributes.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/login";
    }
}