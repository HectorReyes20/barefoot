package com.barefoot.controller;

import com.barefoot.model.Usuario;
import com.barefoot.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Muestra la página de login
     */
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    /**
     * Muestra la página de registro
     */
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    /**
     * Procesa el login
     */
    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Optional<Usuario> usuario = usuarioService.autenticar(email, password);

        if (usuario.isPresent()) {
            Usuario user = usuario.get();

            // Guardar usuario en sesión
            session.setAttribute("usuario", user);
            session.setAttribute("usuarioId", user.getId());
            session.setAttribute("usuarioNombre", user.getNombre());
            session.setAttribute("usuarioRol", user.getRol().toString());

            // Redireccionar según el rol
            if (user.getRol() == Usuario.Rol.ADMIN) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/inicio";
            }
        } else {
            model.addAttribute("error", "Email o contraseña incorrectos");
            return "login";
        }
    }

    /**
     * Procesa el registro
     */
    @PostMapping("/registro")
    public String procesarRegistro(
            @ModelAttribute Usuario usuario,
            Model model,
            HttpSession session) {

        try {
            Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);

            // Iniciar sesión automáticamente después del registro
            session.setAttribute("usuario", nuevoUsuario);
            session.setAttribute("usuarioId", nuevoUsuario.getId());
            session.setAttribute("usuarioNombre", nuevoUsuario.getNombre());
            session.setAttribute("usuarioRol", nuevoUsuario.getRol().toString());

            return "redirect:/inicio";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }

    /**
     * Cierra la sesión
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    /**
     * Página de inicio para usuarios
     */
    @GetMapping("/inicio")
    public String inicio(HttpSession session, Model model) {
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        // Redirigir al catálogo de productos
        return "inicio";
    }

    /**
     * Dashboard para administradores
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        String rol = (String) session.getAttribute("usuarioRol");
        if (!"ADMIN".equals(rol)) {
            return "redirect:/inicio";
        }

        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));
        return "admin/dashboard";
    }
}