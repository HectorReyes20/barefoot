package com.barefoot.controller;

import jakarta.servlet.http.HttpSession; // Asegúrate de importar HttpSession
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Asegúrate de importar Model
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NosotrosController {

    @GetMapping("/nosotros")
    public String mostrarNosotros(HttpSession session, Model model) {
        // Puedes añadir atributos al modelo si los necesitas, como el nombre de usuario
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));
        return "nosotros"; // Nombre del archivo HTML sin la extensión
    }
}