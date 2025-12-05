package com.barefoot.controller;

import com.barefoot.model.Pedido;
import com.barefoot.model.Usuario;
import com.barefoot.repository.PedidoRepository;
import com.barefoot.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/mis-pedidos")
public class MisPedidosController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @GetMapping
    public String listarMisPedidos(HttpSession session, Model model) {

        try {
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                return "redirect:/login";
            }

            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                // Doble chequeo de seguridad
                return "redirect:/login";
            }

            // Obtener todos los pedidos del usuario con sus detalles
            List<Pedido> misPedidos = pedidoRepository.findByUsuarioWithDetalles(usuario);

            model.addAttribute("pedidos", misPedidos);
            model.addAttribute("usuarioNombre", usuario.getNombre());

            // CORRECCIÓN: "pedidos" en lugar de "usuario/pedidos" si el archivo está en templates/
            return "usuario/pedidos";

        } catch (Exception e) {
            log.error("Error en listarMisPedidos: ", e);
            return "redirect:/login";
        }
    }

    @GetMapping("/{id}")
    public String verDetallePedido(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (usuarioId == null) {
                return "redirect:/login";
            }

            Usuario usuario = (Usuario) session.getAttribute("usuario"); // Recuperar usuario para el navbar

            Optional<Pedido> pedidoOpt = pedidoService.obtenerPedidoPorId(id);

            // Verificar si existe y si pertenece al usuario logueado
            if (pedidoOpt.isEmpty() || !pedidoOpt.get().getUsuario().getId().equals(usuarioId)) {
                redirectAttributes.addFlashAttribute("mensaje", "Pedido no encontrado o no autorizado");
                redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
                return "redirect:/mis-pedidos";
            }

            model.addAttribute("pedido", pedidoOpt.get());
            model.addAttribute("usuarioNombre", usuario != null ? usuario.getNombre() : "");

            // CORRECCIÓN: "detalle-pedido" en lugar de "usuario/detalle-pedido"
            return "usuario/detalle-pedido";

        } catch (Exception e) {
            log.error("Error en verDetallePedido: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error al cargar el pedido");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/mis-pedidos";
        }
    }
}
