package com.barefoot.controller;

import com.barefoot.model.Producto;
import com.barefoot.model.Usuario;
import com.barefoot.service.ProductoService;
import com.barefoot.service.UsuarioService;
import com.barefoot.service.CarritoService; // Importar correctamente
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private ProductoService productoService; // Inyectamos el servicio

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

            // Tu lógica de sesión (MANTENER)
            session.setAttribute("usuario", user);
            session.setAttribute("usuarioId", user.getId());
            session.setAttribute("usuarioNombre", user.getNombre());
            session.setAttribute("usuarioRol", user.getRol().toString());

            carritoService.fusionarCarrito(session, user);

            // Tu lógica de carrito pendiente (MANTENER)
            if (session.getAttribute("pendiente_prod_id") != null) {
                return "redirect:/carrito/procesar-pendiente";
            }

            if ("checkout".equals(redirect)) {
                return "redirect:/checkout";
            }

            // --- LÓGICA AGREGADA DE TU COMPAÑERO ---
            if (user.getRol() == Usuario.Rol.ADMIN) {
                return "redirect:/admin/dashboard";
            } else if (user.getRol() == Usuario.Rol.ENCARGADO) { // <--- NUEVO
                return "redirect:/encargado/pedidos";
            }
            // ---------------------------------------

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

            session.setAttribute("usuario", nuevoUsuario);
            session.setAttribute("usuarioId", nuevoUsuario.getId());
            session.setAttribute("usuarioNombre", nuevoUsuario.getNombre());
            session.setAttribute("usuarioRol", nuevoUsuario.getRol().toString());

            if (redirect != null && redirect.equals("checkout")) {
                return "redirect:/checkout";
            }

            return "redirect:/inicio";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            // --- MEJORA DE TU COMPAÑERO ---
            model.addAttribute("usuario", usuario); // Mantiene los datos en el form si falla
            // ------------------------------
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
        // AHORA SÍ FUNCIONARÁ PORQUE AGREGAMOS EL MÉTODO AL SERVICIO
        List<Producto> tendencias = productoService.obtenerProductosRecientes(4);
        model.addAttribute("productosTendencia", tendencias);
        return "inicio";
    }
}