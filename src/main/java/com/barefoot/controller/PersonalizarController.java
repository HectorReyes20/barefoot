package com.barefoot.controller;

import com.barefoot.model.Producto;
import com.barefoot.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PersonalizarController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("/personalizar")
    public String mostrarPersonalizar(HttpSession session, Model model) {
        // Obtenemos productos que sirvan de base para sacar precios e IDs
        // Usaremos el ID 1 como base para Modelo 1 (Urban), ID 4 para Modelo 2 (Sport), ID 7 para Modelo 3 (Classic)
        // Esto es un ejemplo, asegúrate de que estos IDs existan en tu DB 'barefoot_db'
        Producto modelo1 = productoService.obtenerProductoPorId(20L).orElse(new Producto());
        Producto modelo2 = productoService.obtenerProductoPorId(21L).orElse(new Producto());
        Producto modelo3 = productoService.obtenerProductoPorId(22L).orElse(new Producto());

        model.addAttribute("modelo1", modelo1);
        model.addAttribute("modelo2", modelo2);
        model.addAttribute("modelo3", modelo3);

        // Pasamos la variable de sesión para la validación en JS
        model.addAttribute("isLoggedIn", session.getAttribute("usuarioId") != null);

        return "personalizar";
    }
}
