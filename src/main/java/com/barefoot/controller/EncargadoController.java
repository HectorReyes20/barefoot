package com.barefoot.controller;

import com.barefoot.model.MovimientoInventario;
import com.barefoot.model.Pedido;
import com.barefoot.model.Producto;
import com.barefoot.model.Usuario;
import com.barefoot.security.RoleValidator;
import com.barefoot.service.InventarioService;
import com.barefoot.service.PedidoService;
import com.barefoot.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/encargado")
public class EncargadoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String redirectToDashboard() {
        return "redirect:/encargado/pedidos";
    }

    @GetMapping("/pedidos")
    public String listarPedidos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            HttpSession session, Model model) {

        if (!RoleValidator.esAdminOEncargado(session)) {
            return "redirect:/login";
        }

        Page<Pedido> pedidosPage = pedidoService.obtenerPedidosConPaginacion(page, size);
        model.addAttribute("pedidos", pedidosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pedidosPage.getTotalPages());
        model.addAttribute("usuarioRol", RoleValidator.obtenerRol(session));
        model.addAttribute("paginaActiva", "pedidos");

        return "encargado/pedidos";
    }

    @PostMapping("/pedidos/actualizar-estado")
    public String actualizarEstadoPedido(
            @RequestParam Long pedidoId,
            @RequestParam String nuevoEstado,
            HttpSession session, RedirectAttributes redirectAttributes) {

        if (!RoleValidator.esAdminOEncargado(session)) {
            return "redirect:/login";
        }

        try {
            Pedido.EstadoPedido estado = Pedido.EstadoPedido.valueOf(nuevoEstado);

            // Regla de negocio: Encargado solo puede cambiar a PREPARANDO, EN_CAMINO o ENTREGADO
            if (estado != Pedido.EstadoPedido.PREPARANDO && estado != Pedido.EstadoPedido.EN_CAMINO && estado != Pedido.EstadoPedido.ENTREGADO) {
                throw new IllegalArgumentException("Como Encargado, solo puedes cambiar el estado a 'Preparando', 'En Camino' o 'Entregado'.");
            }

            pedidoService.actualizarEstado(pedidoId, estado);
            redirectAttributes.addFlashAttribute("mensaje", "Estado del pedido actualizado correctamente.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            log.error("Error al actualizar estado por encargado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/encargado/pedidos";
    }

    @GetMapping("/inventario")
    public String dashboardInventario(HttpSession session, Model model) {
        if (!RoleValidator.esAdminOEncargado(session)) {
            return "redirect:/login";
        }
        Map<String, Object> stats = inventarioService.obtenerEstadisticasInventario();
        model.addAttribute("stats", stats);
        model.addAttribute("movimientosRecientes", inventarioService.obtenerMovimientosRecientes(5));
        model.addAttribute("productosStockBajo", inventarioService.obtenerProductosStockBajo());
        model.addAttribute("usuarioRol", RoleValidator.obtenerRol(session));
        model.addAttribute("paginaActiva", "inventario");
        return "encargado/inventario-dashboard";
    }

    @GetMapping("/inventario/movimientos")
    public String listarMovimientos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            HttpSession session, Model model) {

        if (!RoleValidator.esAdminOEncargado(session)) {
            return "redirect:/login";
        }

        Page<MovimientoInventario> movimientosPage = inventarioService.obtenerMovimientosConPaginacion(page, size);
        model.addAttribute("movimientos", movimientosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", movimientosPage.getTotalPages());
        model.addAttribute("usuarioRol", RoleValidator.obtenerRol(session));
        model.addAttribute("paginaActiva", "inventario");

        return "encargado/movimientos";
    }

    @GetMapping("/inventario/movimientos/nuevo")
    public String mostrarFormularioMovimiento(HttpSession session, Model model) {
        if (!RoleValidator.esAdminOEncargado(session)) {
            return "redirect:/login";
        }

        List<Producto> productos = productoService.obtenerTodosLosProductos();
        model.addAttribute("movimiento", new MovimientoInventario());
        model.addAttribute("productos", productos);
        model.addAttribute("tiposMovimiento", MovimientoInventario.TipoMovimiento.values());
        model.addAttribute("usuarioRol", RoleValidator.obtenerRol(session));
        model.addAttribute("paginaActiva", "inventario");

        return "encargado/formulario-movimiento";
    }

    @PostMapping("/inventario/movimientos/nuevo")
    public String guardarMovimiento(
            @ModelAttribute MovimientoInventario movimiento,
            HttpSession session, RedirectAttributes redirectAttributes) {

        if (!RoleValidator.esAdminOEncargado(session)) {
            return "redirect:/login";
        }

        try {
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            Usuario usuario = new Usuario();
            usuario.setId(usuarioId);

            Producto producto = productoService.obtenerProductoPorId(movimiento.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            inventarioService.registrarMovimiento(
                producto,
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getMotivo(),
                usuario
            );
            redirectAttributes.addFlashAttribute("mensaje", "Movimiento de inventario registrado con éxito.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            log.error("Error al registrar movimiento por encargado: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/encargado/inventario/movimientos";
    }
}
