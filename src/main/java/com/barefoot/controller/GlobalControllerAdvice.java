package com.barefoot.controller;

import com.barefoot.model.Usuario;
import jakarta.servlet.http.HttpServletRequest; // <--- Importante: Nueva importación
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addAttributes(HttpServletRequest request, HttpSession session, Model model) {

        model.addAttribute("requestURI", request.getRequestURI());

        if (session.getAttribute("usuario") != null) {
            Usuario user = (Usuario) session.getAttribute("usuario");

            model.addAttribute("usuarioLogueado", true);
            model.addAttribute("nombreUsuario", user.getNombre());
            model.addAttribute("usuarioRol", user.getRol().toString());
            model.addAttribute("usuarioEmail", user.getEmail());

        } else {
            model.addAttribute("usuarioLogueado", false);
        }
    }
}