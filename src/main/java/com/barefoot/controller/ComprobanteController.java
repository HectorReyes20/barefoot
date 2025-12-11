package com.barefoot.controller;

import com.barefoot.model.Pedido;
import com.barefoot.service.ComprobanteService;
import com.barefoot.service.PedidoService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/comprobantes")
public class ComprobanteController {

    @Autowired
    private ComprobanteService comprobanteService;

    @Autowired
    private PedidoService pedidoService;

    // ============================================================
    // GENERAR BOLETA (PDF)
    // ============================================================
    @GetMapping("/boleta/{pedidoId}")
    public void generarBoleta(
            @PathVariable Long pedidoId,
            HttpSession session,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        try {
            // Verificar que el usuario esté autenticado
            if (!verificarAcceso(session, pedidoId)) {
                response.sendRedirect("/login");
                return;
            }

            Optional<Pedido> pedidoOpt = pedidoService.obtenerPedidoPorId(pedidoId);
            if (pedidoOpt.isEmpty()) {
                response.sendRedirect("/admin/pedidos");
                return;
            }

            Pedido pedido = pedidoOpt.get();

            // Configurar respuesta HTTP para PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=boleta-" + pedido.getNumeroPedido() + ".pdf");

            // Generar PDF de boleta
            comprobanteService.generarBoleta(pedido, response.getOutputStream());

            log.info("Boleta generada para pedido: {}", pedido.getNumeroPedido());

        } catch (Exception e) {
            log.error("Error al generar boleta: ", e);
            try {
                response.sendRedirect("/admin/pedidos?error=boleta");
            } catch (Exception ex) {
                log.error("Error en redirección: ", ex);
            }
        }
    }

    // ============================================================
    // GENERAR FACTURA (PDF)
    // ============================================================
    @GetMapping("/factura/{pedidoId}")
    public void generarFactura(
            @PathVariable Long pedidoId,
            HttpSession session,
            HttpServletResponse response) {

        try {
            if (!verificarAcceso(session, pedidoId)) {
                response.sendRedirect("/login");
                return;
            }

            Optional<Pedido> pedidoOpt = pedidoService.obtenerPedidoPorId(pedidoId);
            if (pedidoOpt.isEmpty()) {
                response.sendRedirect("/admin/pedidos");
                return;
            }

            Pedido pedido = pedidoOpt.get();

            // Configurar respuesta HTTP para PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=factura-" + pedido.getNumeroPedido() + ".pdf");

            // Generar PDF de factura
            comprobanteService.generarFactura(pedido, response.getOutputStream());

            log.info("Factura generada para pedido: {}", pedido.getNumeroPedido());

        } catch (Exception e) {
            log.error("Error al generar factura: ", e);
            try {
                response.sendRedirect("/admin/pedidos?error=factura");
            } catch (Exception ex) {
                log.error("Error en redirección: ", ex);
            }
        }
    }

    // ============================================================
    // VISTA PREVIA BOLETA (HTML)
    // ============================================================
    @GetMapping("/preview/boleta/{pedidoId}")
    public String previsualizarBoleta(
            @PathVariable Long pedidoId,
            HttpSession session,
            org.springframework.ui.Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (!verificarAcceso(session, pedidoId)) {
                return "redirect:/login";
            }

            Optional<Pedido> pedidoOpt = pedidoService.obtenerPedidoPorId(pedidoId);
            if (pedidoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensaje", "Pedido no encontrado");
                redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
                return "redirect:/admin/pedidos";
            }

            model.addAttribute("pedido", pedidoOpt.get());
            model.addAttribute("tipoComprobante", "BOLETA");

            return "admin/comprobantes/preview";

        } catch (Exception e) {
            log.error("Error en preview boleta: ", e);
            return "redirect:/admin/pedidos";
        }
    }

    // ============================================================
    // VISTA PREVIA FACTURA (HTML)
    // ============================================================
    @GetMapping("/preview/factura/{pedidoId}")
    public String previsualizarFactura(
            @PathVariable Long pedidoId,
            HttpSession session,
            org.springframework.ui.Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (!verificarAcceso(session, pedidoId)) {
                return "redirect:/login";
            }

            Optional<Pedido> pedidoOpt = pedidoService.obtenerPedidoPorId(pedidoId);
            if (pedidoOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensaje", "Pedido no encontrado");
                redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
                return "redirect:/admin/pedidos";
            }

            model.addAttribute("pedido", pedidoOpt.get());
            model.addAttribute("tipoComprobante", "FACTURA");

            return "admin/comprobantes/preview";

        } catch (Exception e) {
            log.error("Error en preview factura: ", e);
            return "redirect:/admin/pedidos";
        }
    }

    // ============================================================
    // HELPER: VERIFICAR ACCESO
    // ============================================================
    private boolean verificarAcceso(HttpSession session, Long pedidoId) {
        String rol = (String) session.getAttribute("usuarioRol");

        // Los admins pueden generar cualquier comprobante
        if ("ADMIN".equals(rol)) {
            return true;
        }

        // Los usuarios solo pueden generar sus propios comprobantes
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId != null) {
            Optional<Pedido> pedidoOpt = pedidoService.obtenerPedidoPorId(pedidoId);
            return pedidoOpt.isPresent() &&
                    pedidoOpt.get().getUsuario().getId().equals(usuarioId);
        }

        return false;
    }
}