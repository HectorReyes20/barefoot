package com.barefoot.controller;

import com.barefoot.model.Favorito;
import com.barefoot.model.Usuario;
import com.barefoot.repository.UsuarioRepository;
import com.barefoot.service.FavoritoService;
import com.barefoot.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class UsuarioAreaController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder; // Inyectar Encoder
    @Autowired
    private FavoritoService favoritoService;
    @Autowired // <--- 1. INYECTAR PEDIDO SERVICE
    private PedidoService pedidoService;

    // --- SECCIÓN PERFIL ---
    @GetMapping("/perfil")
    public String verPerfil(HttpSession session, Model model) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

        // 2. CALCULAR CONTADORES
        int cantidadFavoritos = favoritoService.listarFavoritos(usuarioId).size();
        int cantidadPedidos = pedidoService.obtenerPedidosDeUsuario(usuario).size();

        // 3. PASAR AL MODELO
        model.addAttribute("usuario", usuario);
        model.addAttribute("cantidadFavoritos", cantidadFavoritos);
        model.addAttribute("cantidadPedidos", cantidadPedidos);

        return "usuario/perfil";
    }

    @PostMapping("/perfil/cambiar-password")
    public String cambiarPassword(@RequestParam String currentPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        Usuario usuario = usuarioRepository.findById(usuarioId).get();

        // 1. Verificar contraseña actual
        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            redirectAttributes.addFlashAttribute("mensaje", "La contraseña actual es incorrecta.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/perfil";
        }

        // 2. Verificar coincidencia de nueva
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("mensaje", "Las contraseñas nuevas no coinciden.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/perfil";
        }

        // 3. Guardar
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("mensaje", "Contraseña actualizada con éxito.");
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/perfil";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(
            @ModelAttribute Usuario usuarioForm,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";

        // Buscamos el usuario real de la BD para no perder datos (password, rol, etc)
        Usuario usuarioDB = usuarioRepository.findById(usuarioId).get();

        // Actualizamos solo lo permitido
        usuarioDB.setNombre(usuarioForm.getNombre());
        usuarioDB.setApellido(usuarioForm.getApellido());
        usuarioDB.setTelefono(usuarioForm.getTelefono());
        // El email generalmente no se cambia tan fácil por seguridad, pero puedes agregarlo si quieres

        usuarioRepository.save(usuarioDB);

        // Actualizamos nombre en sesión por si cambió
        session.setAttribute("usuarioNombre", usuarioForm.getNombre());

        redirectAttributes.addFlashAttribute("mensaje", "Datos actualizados correctamente");
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        return "redirect:/perfil";
    }

    // --- SECCIÓN FAVORITOS ---
    @GetMapping("/favoritos")
    public String verFavoritos(HttpSession session, Model model) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";

        List<Favorito> favoritos = favoritoService.listarFavoritos(usuarioId);
        model.addAttribute("favoritos", favoritos);

        return "usuario/favoritos"; // Crearemos esta vista
    }

    // Acción para dar like/dislike (funciona como un interruptor)
    @GetMapping("/favoritos/toggle/{productoId}")
    public String toggleFavorito(@PathVariable Long productoId, HttpSession session, RedirectAttributes attrs, @RequestHeader(value = "referer", required = false) String referer) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";

        boolean esFavorito = favoritoService.toggleFavorito(usuarioId, productoId);

        String msg = esFavorito ? "Añadido a favoritos" : "Eliminado de favoritos";
        attrs.addFlashAttribute("mensaje", msg);
        attrs.addFlashAttribute("tipoMensaje", "success");

        // Volver a la página desde donde se hizo click
        return "redirect:" + (referer != null ? referer : "/productos");
    }
}