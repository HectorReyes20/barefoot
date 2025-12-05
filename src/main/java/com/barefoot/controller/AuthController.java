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
            @RequestParam(required = false) String redirect,
            HttpSession session,
            Model model) {

        Optional<Usuario> usuario = usuarioService.autenticar(email, password);

        if (usuario.isPresent()) {
            Usuario user = usuario.get();

            session.setAttribute("usuario", user);
            session.setAttribute("usuarioId", user.getId());
            session.setAttribute("usuarioNombre", user.getNombre());
            session.setAttribute("usuarioRol", user.getRol().toString());

            // Si viene de checkout, redirigir a checkout
            if (redirect != null && redirect.equals("checkout")) {
                return "redirect:/checkout";
            }

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
            @RequestParam(required = false) String redirect,
            Model model,
            HttpSession session) {

        try {
            Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);

            session.setAttribute("usuario", nuevoUsuario);
            session.setAttribute("usuarioId", nuevoUsuario.getId());
            session.setAttribute("usuarioNombre", nuevoUsuario.getNombre());
            session.setAttribute("usuarioRol", nuevoUsuario.getRol().toString());

            // Si viene de checkout, redirigir a checkout
            if (redirect != null && redirect.equals("checkout")) {
                return "redirect:/checkout";
            }

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
        return "inicio";
    }
}
