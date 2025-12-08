package com.barefoot.controller;

import com.barefoot.model.Transaccion;
import com.barefoot.repository.TransaccionRepository;
import com.barefoot.service.PagoManualService;
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

    /**
     * Dashboard de pagos (Dashboard Inteligente)
     */
    @GetMapping
    public String listarPagosPendientes(HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/login";

        // 1. Obtener todo ordenado por fecha
        List<Transaccion> todas = transaccionRepository.findAll(
                Sort.by(Sort.Direction.DESC, "fechaCreacion"));

        // 2. LO IMPORTANTE: Solo mostrar Yapes Pendientes para aprobar
        // (Stripe se aprueba solo, así que no ensucia tu lista de pendientes)
        List<Transaccion> yapePendientes = todas.stream()
                .filter(t -> t.getPasarela() == Transaccion.Pasarela.YAPE)
                .filter(t -> t.getEstado() == Transaccion.EstadoTransaccion.PENDIENTE)
                .collect(Collectors.toList());

        // 3. Historial de Completados (últimos 30)
        List<Transaccion> completadas = todas.stream()
                .filter(t -> t.getEstado() == Transaccion.EstadoTransaccion.COMPLETADO)
                .limit(30)
                .collect(Collectors.toList());

        // 4. Historial de Fallidos (últimos 15)
        List<Transaccion> fallidas = todas.stream()
                .filter(t -> t.getEstado() == Transaccion.EstadoTransaccion.FALLIDO)
                .limit(15)
                .collect(Collectors.toList());

        // 5. Estadísticas rápidas para el Admin
        double montoPorCobrar = yapePendientes.stream()
                .mapToDouble(Transaccion::getMonto).sum();

        long pagosStripe = completadas.stream()
                .filter(t -> t.getPasarela() == Transaccion.Pasarela.STRIPE).count();

        model.addAttribute("yapePendientes", yapePendientes);
        model.addAttribute("completadas", completadas);
        model.addAttribute("fallidas", fallidas);
        model.addAttribute("totalYapePendientes", yapePendientes.size());
        model.addAttribute("montoPorCobrar", montoPorCobrar);
        model.addAttribute("pagosStripe", pagosStripe);
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "admin/pagos/lista";
    }

    /**
     * Ver detalle
     */
    @GetMapping("/{id}")
    public String verDetalleTransaccion(@PathVariable Long id, HttpSession session, Model model) {
        if (!esAdmin(session)) return "redirect:/login";

        return transaccionRepository.findById(id)
                .map(t -> {
                    model.addAttribute("transaccion", t);
                    model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));
                    return "admin/pagos/detalle";
                })
                .orElse("redirect:/admin/pagos");
    }

    /**
     * Confirmar pago (CORREGIDO)
     */
    @PostMapping("/{id}/confirmar")
    public String confirmarPago(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) return "redirect:/login";

        try {
            // CORRECCIÓN: Usamos el método genérico del servicio
            Transaccion transaccion = pagoManualService.confirmarPagoManual(id);

            redirectAttributes.addFlashAttribute("mensaje",
                    "✅ Pago confirmado. Pedido #" + transaccion.getPedido().getNumeroPedido());
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            log.error("Error confirmando pago: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/pagos";
    }

    /**
     * Rechazar pago (CORREGIDO)
     */
    @PostMapping("/{id}/rechazar")
    public String rechazarPago(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) return "redirect:/login";

        try {
            String motivoFinal = (motivo != null && !motivo.isEmpty()) ? motivo : "Código inválido";

            // CORRECCIÓN: Usamos el método genérico del servicio
            Transaccion transaccion = pagoManualService.rechazarPagoManual(id, motivoFinal);

            redirectAttributes.addFlashAttribute("mensaje",
                    "🚫 Pago rechazado y pedido cancelado.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");

        } catch (Exception e) {
            log.error("Error rechazando pago: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/pagos";
    }

    /**
     * Buscar transacción (Por código de aprobación)
     */
    @GetMapping("/buscar")
    public String buscarTransaccion(
            @RequestParam String query,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!esAdmin(session)) return "redirect:/login";

        // Buscar por el código (referencia externa)
        List<Transaccion> resultados = transaccionRepository
                .findByReferenciaExternaContainingIgnoreCase(query);

        if (resultados.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "No se encontró el código: " + query);
            redirectAttributes.addFlashAttribute("tipoMensaje", "info");
            return "redirect:/admin/pagos";
        }

        // Reutilizamos la vista lista, pero solo con los resultados encontrados
        model.addAttribute("yapePendientes", resultados); // Ponemos resultados aquí para que se vean en la tabla
        model.addAttribute("completadas", List.of()); // Ocultamos las otras tablas en búsqueda
        model.addAttribute("fallidas", List.of());
        model.addAttribute("busqueda", query);

        return "admin/pagos/lista";
    }

    private boolean esAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ADMIN".equals(rol);
    }
}