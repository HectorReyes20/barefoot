package com.barefoot.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NosotrosController {

    @GetMapping("/nosotros")
    public String mostrarNosotros(HttpSession session, Model model) {
        // Agregar el nombre del usuario a la vista (si existe)
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));
        return "nosotros";
    }
}
