package com.barefoot.controller;

import com.barefoot.model.Usuario;
import com.barefoot.service.UsuarioService;
import com.barefoot.service.CarritoService;
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

    @Autowired
    private CarritoService carritoService;

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

        Optional<Usuario> usuarioOpt = usuarioService.autenticar(email, password);

        if (usuarioOpt.isPresent()) {
            Usuario user = usuarioOpt.get();
            session.setAttribute("usuarioId", user.getId());
            session.setAttribute("usuarioNombre", user.getNombre());
            session.setAttribute("usuarioRol", user.getRol().toString());
            carritoService.fusionarCarrito(session, user);

            if ("checkout".equals(redirect)) {
                return "redirect:/checkout";
            }

            // Lógica de redirección por rol
            if (user.getRol() == Usuario.Rol.ADMIN) {
                return "redirect:/admin/dashboard";
            } else if (user.getRol() == Usuario.Rol.ENCARGADO) {
                return "redirect:/encargado/pedidos"; // Redirige al encargado a su vista de pedidos
            }
            
            return "redirect:/inicio";
        } else {
            model.addAttribute("error", "Credenciales inválidas");
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

            session.setAttribute("usuarioId", nuevoUsuario.getId());
            session.setAttribute("usuarioNombre", nuevoUsuario.getNombre());
            session.setAttribute("usuarioRol", nuevoUsuario.getRol().toString());

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
