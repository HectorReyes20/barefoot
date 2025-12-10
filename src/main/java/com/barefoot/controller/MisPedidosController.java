package com.barefoot.controller;

import com.barefoot.model.Pedido;
import com.barefoot.model.Transaccion;
import com.barefoot.model.Usuario;
import com.barefoot.repository.PedidoRepository;
import com.barefoot.repository.TransaccionRepository;
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
    private TransaccionRepository transaccionRepository;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    // 1. LISTAR TODOS LOS PEDIDOS
    @GetMapping
    public String listarMisPedidos(HttpSession session, Model model) {
        try {
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (usuarioId == null) return "redirect:/login";

            Usuario usuario = new Usuario();
            usuario.setId(usuarioId);

            // Usamos el método optimizado que ya tienes en tu Repositorio
            List<Pedido> misPedidos = pedidoRepository.findByUsuarioWithDetalles(usuario);

            model.addAttribute("pedidos", misPedidos);
            model.addAttribute("usuarioNombre", session.getAttribute("usuarioNombre")); // Usamos el de la sesión

            return "usuario/pedidos";

        } catch (Exception e) {
            log.error("Error en listarMisPedidos: ", e);
            return "redirect:/login";
        }
    }

    // 2. VER DETALLE DE UN PEDIDO
    @GetMapping("/{id}")
    public String verDetallePedido(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (usuarioId == null) return "redirect:/login";

            // Buscar el pedido
            Optional<Pedido> pedidoOpt = pedidoService.obtenerPedidoPorId(id);

            // Validación de seguridad: ¿Existe? ¿Es mío?
            if (pedidoOpt.isEmpty() || !pedidoOpt.get().getUsuario().getId().equals(usuarioId)) {
                redirectAttributes.addFlashAttribute("mensaje", "Pedido no encontrado o no autorizado");
                redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
                return "redirect:/mis-pedidos";
            }

            Pedido pedido = pedidoOpt.get();

            // Buscar información del pago (Solo para mostrar el recibo/estado)
            // Ya no es para "completar pago", sino para ver "cómo pagó"
            Optional<Transaccion> transaccion = transaccionRepository
                    .findFirstByPedidoOrderByFechaCreacionDesc(pedido);

            model.addAttribute("pedido", pedido);
            model.addAttribute("transaccion", transaccion.orElse(null));

            // Pasamos el nombre para el navbar
            model.addAttribute("usuarioNombre", session.getAttribute("usuarioNombre"));

            return "usuario/detalle-pedido";

        } catch (Exception e) {
            log.error("Error en verDetallePedido: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error al cargar el detalle");
            return "redirect:/mis-pedidos";
        }
    }
}