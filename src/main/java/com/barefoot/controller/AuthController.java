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

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Optional<Usuario> usuario = usuarioService.autenticar(email, password);

        if (usuario.isPresent()) {
            Usuario user = usuario.get();

            // Guardar usuario en sesión (¡Esto es importante!)
            session.setAttribute("usuario", user); // Guardamos el objeto entero
            session.setAttribute("usuarioId", user.getId());
            session.setAttribute("usuarioNombre", user.getNombre());
            session.setAttribute("usuarioRol", user.getRol().toString());

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

    @PostMapping("/registro")
    public String procesarRegistro(
            @ModelAttribute Usuario usuario,
            Model model,
            HttpSession session) {

        try {
            Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);

            // Iniciar sesión automáticamente
            session.setAttribute("usuario", nuevoUsuario); // Guardamos el objeto entero
            session.setAttribute("usuarioId", nuevoUsuario.getId());
            session.setAttribute("usuarioNombre", nuevoUsuario.getNombre());
            session.setAttribute("usuarioRol", nuevoUsuario.getRol().toString());

            return "redirect:/inicio";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ==========================================================
    // MÉTODO MODIFICADO (PÚBLICO)
    // ==========================================================
    /**
     * Página de inicio (pública, no requiere login)
     */
    @GetMapping("/inicio")
    public String inicio(HttpSession session, Model model) {

        // Comprobar si hay un usuario en sesión
        if (session.getAttribute("usuario") != null) {
            Usuario user = (Usuario) session.getAttribute("usuario");

            // Pasamos los datos al modelo para que la vista (HTML) los use
            model.addAttribute("usuarioLogueado", true);
            model.addAttribute("nombreUsuario", user.getNombre());
            model.addAttribute("usuarioRol", user.getRol().toString());
            model.addAttribute("usuarioEmail", user.getEmail());

        } else {
            // Si no hay sesión, pasamos el indicador en falso
            model.addAttribute("usuarioLogueado", false);
        }

        // Siempre mostramos la página de inicio
        return "inicio";
    }

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