package com.barefoot.controller;

import com.barefoot.model.Pedido;
import com.barefoot.model.Transaccion;
import com.barefoot.service.PagoManualService;
import com.barefoot.service.PedidoService;
import com.barefoot.repository.TransaccionRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin/pagos")
public class AdminPagosController {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private PagoManualService pagoManualService;

    @Autowired
    private PedidoService pedidoService;

    /**
     * Dashboard de pagos pendientes
     */
    @GetMapping
    public String listarPagosPendientes(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        // Obtener todas las transacciones
        List<Transaccion> todasTransacciones = transaccionRepository.findAll(
                Sort.by(Sort.Direction.DESC, "fechaCreacion"));

        // Separar por estado
        List<Transaccion> pendientes = todasTransacciones.stream()
                .filter(t -> t.getEstado() == Transaccion.EstadoTransaccion.PENDIENTE)
                .collect(Collectors.toList());

        List<Transaccion> completadas = todasTransacciones.stream()
                .filter(t -> t.getEstado() == Transaccion.EstadoTransaccion.COMPLETADO)
                .limit(20) // Solo las últimas 20
                .collect(Collectors.toList());

        List<Transaccion> fallidas = todasTransacciones.stream()
                .filter(t -> t.getEstado() == Transaccion.EstadoTransaccion.FALLIDO)
                .limit(10)
                .collect(Collectors.toList());

        // Estadísticas
        long totalPendientes = pendientes.size();
        long totalCompletadas = transaccionRepository.countByEstado(Transaccion.EstadoTransaccion.COMPLETADO);

        double montoPendiente = pendientes.stream()
                .mapToDouble(Transaccion::getMonto)
                .sum();

        model.addAttribute("pendientes", pendientes);
        model.addAttribute("completadas", completadas);
        model.addAttribute("fallidas", fallidas);
        model.addAttribute("totalPendientes", totalPendientes);
        model.addAttribute("totalCompletadas", totalCompletadas);
        model.addAttribute("montoPendiente", montoPendiente);
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "admin/pagos/lista";
    }

    /**
     * Ver detalle de una transacción
     */
    @GetMapping("/{id}")
    public String verDetalleTransaccion(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            Transaccion transaccion = transaccionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

            model.addAttribute("transaccion", transaccion);
            model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

            return "admin/pagos/detalle";

        } catch (Exception e) {
            log.error("Error al ver detalle de transacción: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/admin/pagos";
        }
    }

    /**
     * Confirmar pago manual
     */
    @PostMapping("/{id}/confirmar")
    public String confirmarPago(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            Transaccion transaccion = pagoManualService.confirmarPagoManual(id);

            redirectAttributes.addFlashAttribute("mensaje",
                    "Pago confirmado exitosamente. Pedido #" + transaccion.getPedido().getNumeroPedido());
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            log.error("Error al confirmar pago: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/pagos";
    }

    /**
     * Rechazar pago manual
     */
    @PostMapping("/{id}/rechazar")
    public String rechazarPago(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            String motivoRechazo = (motivo != null && !motivo.isEmpty())
                    ? motivo
                    : "Pago no verificado";

            Transaccion transaccion = pagoManualService.rechazarPagoManual(id, motivoRechazo);

            redirectAttributes.addFlashAttribute("mensaje",
                    "Pago rechazado. Pedido #" + transaccion.getPedido().getNumeroPedido() + " cancelado.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");

        } catch (Exception e) {
            log.error("Error al rechazar pago: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/pagos";
    }

    /**
     * Buscar transacción por número de operación o pedido
     */
    @GetMapping("/buscar")
    public String buscarTransaccion(
            @RequestParam String query,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            // Buscar por referencia externa (número de operación)
            List<Transaccion> resultados = transaccionRepository
                    .findByReferenciaExternaContainingIgnoreCase(query);

            if (resultados.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensaje",
                        "No se encontraron transacciones con: " + query);
                redirectAttributes.addFlashAttribute("tipoMensaje", "info");
                return "redirect:/admin/pagos";
            }

            model.addAttribute("pendientes", resultados);
            model.addAttribute("busqueda", query);
            model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

            return "admin/pagos/lista";

        } catch (Exception e) {
            log.error("Error al buscar transacción: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error en la búsqueda");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/admin/pagos";
        }
    }

    private boolean esAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ADMIN".equals(rol);
    }
}