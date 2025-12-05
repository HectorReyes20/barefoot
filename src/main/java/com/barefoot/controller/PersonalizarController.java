package com.barefoot.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PersonalizarController {

    @GetMapping("/personalizar")
    public String mostrarPersonalizar(HttpSession session, Model model) {
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));
        return "personalizar"; // Nombre del archivo HTML
    }
}
