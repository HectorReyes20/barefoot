package com.barefoot.controller;

import com.barefoot.model.*;
import com.barefoot.service.InventarioService;
import com.barefoot.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoService productoService;

    // Dashboard de inventario
    @GetMapping
    public String mostrarInventario(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session,
            Model model) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        // Estadísticas generales
        Map<String, Object> estadisticas = inventarioService.obtenerEstadisticasInventario();
        model.addAttribute("estadisticas", estadisticas);

        // Productos con stock bajo
        List<Map<String, Object>> productosStockBajo = inventarioService.obtenerProductosStockBajo();
        model.addAttribute("productosStockBajo", productosStockBajo);

        // Alertas activas
        List<AlertaInventario> alertas = inventarioService.obtenerAlertasNoLeidas();
        model.addAttribute("alertas", alertas);
        model.addAttribute("cantidadAlertas", alertas.size());

        // Movimientos recientes
        List<MovimientoInventario> movimientosRecientes = inventarioService.obtenerMovimientosRecientes(10);
        model.addAttribute("movimientosRecientes", movimientosRecientes);

        // Movimientos por tipo
        Map<String, Long> movimientosPorTipo = inventarioService.obtenerMovimientosPorTipo();
        model.addAttribute("movimientosPorTipo", movimientosPorTipo);

        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "admin/inventario/dashboard";
    }

    // Historial de movimientos
    @GetMapping("/movimientos")
    public String listarMovimientos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String tipo,
            HttpSession session,
            Model model) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        Page<MovimientoInventario> movimientosPage = inventarioService.obtenerMovimientosConPaginacion(page, size);

        model.addAttribute("movimientos", movimientosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", movimientosPage.getTotalPages());
        model.addAttribute("tiposMovimiento", MovimientoInventario.TipoMovimiento.values());
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "admin/inventario/movimientos";
    }

    // Formulario para registrar movimiento
    @GetMapping("/movimientos/nuevo")
    public String mostrarFormularioMovimiento(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        List<Producto> productos = productoService.obtenerProductosActivos();
        model.addAttribute("productos", productos);
        model.addAttribute("tiposMovimiento", MovimientoInventario.TipoMovimiento.values());
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "admin/inventario/formulario-movimiento";
    }

    // Registrar movimiento
    @PostMapping("/movimientos/registrar")
    public String registrarMovimiento(
            @RequestParam Long productoId,
            @RequestParam String tipo,
            @RequestParam Integer cantidad,
            @RequestParam String motivo,
            @RequestParam(required = false) String numeroDocumento,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            Optional<Producto> productoOpt = productoService.obtenerProductoPorId(productoId);
            if (productoOpt.isEmpty()) {
                throw new RuntimeException("Producto no encontrado");
            }

            Producto producto = productoOpt.get();
            MovimientoInventario.TipoMovimiento tipoMovimiento = MovimientoInventario.TipoMovimiento.valueOf(tipo);

            Usuario usuario = (Usuario) session.getAttribute("usuario");

            inventarioService.registrarMovimiento(producto, tipoMovimiento, cantidad, motivo, numeroDocumento, usuario);

            redirectAttributes.addFlashAttribute("mensaje", "Movimiento registrado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/inventario";
    }

    // Ver detalles de un producto en inventario
    @GetMapping("/producto/{id}")
    public String verDetalleProducto(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            Map<String, Object> estadisticas = inventarioService.obtenerEstadisticasProducto(id);
            model.addAllAttributes(estadisticas);
            model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

            return "admin/inventario/detalle-producto";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/admin/inventario";
        }
    }

    // Gestión de alertas
    @GetMapping("/alertas")
    public String listarAlertas(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        List<AlertaInventario> alertasActivas = inventarioService.obtenerAlertasActivas();
        List<AlertaInventario> alertasCriticas = inventarioService.obtenerAlertasCriticas();

        model.addAttribute("alertasActivas", alertasActivas);
        model.addAttribute("alertasCriticas", alertasCriticas);
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "admin/inventario/alertas";
    }

    // Marcar alerta como leída
    @PostMapping("/alertas/{id}/marcar-leida")
    public String marcarAlertaLeida(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            inventarioService.marcarAlertaComoLeida(id, usuario);

            redirectAttributes.addFlashAttribute("mensaje", "Alerta marcada como leída");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/inventario/alertas";
    }

    // Marcar todas las alertas como leídas
    @PostMapping("/alertas/marcar-todas-leidas")
    public String marcarTodasAlertasLeidas(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            inventarioService.marcarTodasAlertasComoLeidas(usuario);

            redirectAttributes.addFlashAttribute("mensaje", "Todas las alertas han sido marcadas como leídas");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/inventario/alertas";
    }

    // Desactivar alerta
    @PostMapping("/alertas/{id}/desactivar")
    public String desactivarAlerta(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            inventarioService.desactivarAlerta(id);

            redirectAttributes.addFlashAttribute("mensaje", "Alerta desactivada");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/inventario/alertas";
    }

    private boolean esAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ADMIN".equals(rol);
    }
}