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


    @PostMapping("/registro")
    public String procesarRegistro(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam String password,
            Model model,
            HttpSession session) {

        try {
            // Crear objeto usuario
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEmail(email);
            usuario.setTelefono(telefono);
            usuario.setPassword(password);

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


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }


    @GetMapping("/inicio")
    public String inicio(HttpSession session, Model model) {
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));
        // CAMBIO: Ahora retorna directamente la vista "inicio"
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