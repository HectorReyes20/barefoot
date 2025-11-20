package com.barefoot.controller;

import jakarta.servlet.http.HttpSession; // Asegúrate de importar HttpSession
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Asegúrate de importar Model
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PersonalizarController {

    @GetMapping("/personalizar")
    public String mostrarPersonalizar(HttpSession session, Model model) {
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));
        return "personalizar"; // Nombre del archivo HTML
    }

    // Aquí podrías añadir un @PostMapping para recibir los datos del formulario
    // cuando el usuario presione "Agregar al Carrito", si implementas esa lógica.
}