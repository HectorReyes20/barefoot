package com.barefoot.controller;

import com.barefoot.model.Pedido;
import com.barefoot.service.PedidoService;
import com.barefoot.security.RoleValidator;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/admin/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public String listarPedidos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String estado,
            HttpSession session,
            Model model) {

        if (!RoleValidator.esAdminOEncargado(session)) {
            return "redirect:/login";
        }

        Page<Pedido> pedidosPage;

        if (estado != null && !estado.isEmpty()) {
            try {
                Pedido.EstadoPedido estadoPedido = Pedido.EstadoPedido.valueOf(estado);
                List<Pedido> pedidos = pedidoService.obtenerPedidosPorEstado(estadoPedido);
                model.addAttribute("pedidos", pedidos);
                model.addAttribute("estadoFiltro", estado);
            } catch (IllegalArgumentException e) {
                pedidosPage = pedidoService.obtenerPedidosConPaginacion(page, size);
                model.addAttribute("pedidos", pedidosPage.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", pedidosPage.getTotalPages());
            }
        } else {
            pedidosPage = pedidoService.obtenerPedidosConPaginacion(page, size);
            model.addAttribute("pedidos", pedidosPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", pedidosPage.getTotalPages());
        }

        model.addAttribute("totalPedidos", pedidoService.obtenerTodosPedidos().size());
        model.addAttribute("pedidosConfirmados", pedidoService.contarPedidosPorEstado(Pedido.EstadoPedido.CONFIRMADO));
        model.addAttribute("pedidosEnviados", pedidoService.contarPedidosPorEstado(Pedido.EstadoPedido.EN_CAMINO));
        model.addAttribute("pedidosEntregados", pedidoService.contarPedidosPorEstado(Pedido.EstadoPedido.ENTREGADO));
        model.addAttribute("ventasTotales", pedidoService.calcularTotalVentas());
        model.addAttribute("estados", Pedido.EstadoPedido.values());
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));
        model.addAttribute("usuarioRol", RoleValidator.obtenerRol(session));

        return "admin/pedidos/lista";
    }

    @GetMapping("/{id}")
    public String verDetallePedido(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!RoleValidator.esAdminOEncargado(session)) {
            return "redirect:/login";
        }

        Optional<Pedido> pedido = pedidoService.obtenerPedidoPorId(id);

        if (pedido.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Pedido no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/admin/pedidos";
        }

        model.addAttribute("pedido", pedido.get());
        model.addAttribute("estados", Pedido.EstadoPedido.values());
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));
        model.addAttribute("usuarioRol", RoleValidator.obtenerRol(session));

        return "admin/pedidos/detalle";
    }

    @PostMapping("/{id}/actualizar-estado")
    public String actualizarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!RoleValidator.esAdminOEncargado(session)) {
            return "redirect:/login";
        }

        try {
            Optional<Pedido> pedidoOpt = pedidoService.obtenerPedidoPorId(id);
            if (pedidoOpt.isEmpty()) {
                throw new RuntimeException("Pedido no encontrado");
            }

            Pedido pedido = pedidoOpt.get();
            Pedido.EstadoPedido estado = Pedido.EstadoPedido.valueOf(nuevoEstado);

            if (RoleValidator.esEncargado(session)) {
                boolean transicionValida = (pedido.getEstado() == Pedido.EstadoPedido.CONFIRMADO && estado == Pedido.EstadoPedido.PREPARANDO) ||
                                           (pedido.getEstado() == Pedido.EstadoPedido.PREPARANDO && estado == Pedido.EstadoPedido.EN_CAMINO);
                if (!transicionValida) {
                    throw new RuntimeException("Como ENCARGADO, solo puedes cambiar estados de CONFIRMADO -> PREPARANDO -> EN_CAMINO");
                }
            }

            pedidoService.actualizarEstado(id, estado);
            redirectAttributes.addFlashAttribute("mensaje", "Estado actualizado correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/pedidos/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelarPedido(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!RoleValidator.esAdmin(session)) {
            redirectAttributes.addFlashAttribute("mensaje", "No tienes permisos para cancelar pedidos");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/admin/pedidos";
        }

        try {
            pedidoService.cancelarPedido(id, motivo);
            redirectAttributes.addFlashAttribute("mensaje", "Pedido cancelado correctamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/pedidos/" + id;
    }

    @GetMapping("/buscar")
    public String buscarPedido(
            @RequestParam String numero,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!RoleValidator.esAdminOEncargado(session)) {
            return "redirect:/login";
        }

        Optional<Pedido> pedido = pedidoService.obtenerPedidoPorNumero(numero);

        if (pedido.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "No se encontró el pedido: " + numero);
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            return "redirect:/admin/pedidos";
        }

        return "redirect:/admin/pedidos/" + pedido.get().getId();
    }
}
