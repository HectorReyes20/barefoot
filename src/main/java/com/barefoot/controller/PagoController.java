package com.barefoot.controller;

import com.barefoot.model.Pedido;
import com.barefoot.model.Transaccion;
import com.barefoot.service.PagoManualService;
import com.barefoot.service.PagoService;
import com.barefoot.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/pago")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private PagoManualService pagoManualService;

    @Autowired
    private PedidoService pedidoService;

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    @GetMapping("/procesar/{pedidoId}")
    public String mostrarPaginaPago(
            @PathVariable Long pedidoId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("usuarioId") == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Debes iniciar sesión");
            return "redirect:/login";
        }

        try {
            Pedido pedido = pedidoService.obtenerPedidoPorId(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (!pedido.getUsuario().getId().equals(usuarioId)) {
                throw new RuntimeException("No tienes permiso para acceder a este pedido");
            }

            if (pedido.getEstado() != Pedido.EstadoPedido.CONFIRMADO) {
                throw new RuntimeException("Este pedido ya fue procesado o no es válido para pago.");
            }

            model.addAttribute("pedido", pedido);
            model.addAttribute("stripePublicKey", stripePublicKey);

            return "pago/procesar";

        } catch (Exception e) {
            log.error("Error al mostrar página de pago: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/mis-pedidos";
        }
    }

    @PostMapping("/api/iniciar-stripe")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> iniciarPagoStripe(
            @RequestParam Long pedidoId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (session.getAttribute("usuarioId") == null) {
                response.put("error", "No estás autenticado");
                return ResponseEntity.badRequest().body(response);
            }

            Pedido pedido = pedidoService.obtenerPedidoPorId(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (!pedido.getUsuario().getId().equals(usuarioId)) {
                response.put("error", "No tienes permiso para este pedido");
                return ResponseEntity.badRequest().body(response);
            }

            Transaccion transaccion = pagoService.iniciarPagoStripe(pedido);

            response.put("exito", true);
            response.put("clientSecret", transaccion.getTokenPago());
            response.put("transaccionId", transaccion.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al iniciar pago Stripe: ", e);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/api/confirmar-stripe")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmarPagoStripe(
            @RequestParam String paymentIntentId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (session.getAttribute("usuarioId") == null) {
                response.put("error", "No estás autenticado");
                return ResponseEntity.badRequest().body(response);
            }

            Transaccion transaccion = pagoService.confirmarPago(paymentIntentId);

            if (transaccion.getEstado() == Transaccion.EstadoTransaccion.COMPLETADO) {
                response.put("exito", true);
                response.put("mensaje", "Pago completado exitosamente");
                response.put("redirect", "/checkout/confirmacion/" + transaccion.getPedido().getId());
            } else {
                response.put("error", "El pago no se completó correctamente");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al confirmar pago: ", e);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/webhook/stripe")
    @ResponseBody
    public ResponseEntity<String> webhookStripe(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Webhook recibido de Stripe");
        return ResponseEntity.ok("success");
    }

    @GetMapping("/cancelar/{pedidoId}")
    public String cancelarPago(
            @PathVariable Long pedidoId,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("mensaje", "Pago cancelado");
        redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
        return "redirect:/checkout";
    }

    @GetMapping("/manual/{pedidoId}")
    public String mostrarPagoManual(
            @PathVariable Long pedidoId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("usuarioId") == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Debes iniciar sesión");
            return "redirect:/login";
        }

        try {
            Pedido pedido = pedidoService.obtenerPedidoPorId(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (!pedido.getUsuario().getId().equals(usuarioId)) {
                throw new RuntimeException("No tienes permiso para acceder a este pedido");
            }

            if (pedido.getEstado() != Pedido.EstadoPedido.CONFIRMADO) {
                throw new RuntimeException("Este pedido ya fue procesado o no es válido para pago.");
            }

            String metodoPago = pedido.getMetodoPago().name();
            Map<String, String> infoPago = pagoManualService.obtenerInformacionPago(metodoPago);

            model.addAttribute("pedido", pedido);
            model.addAttribute("metodoPago", metodoPago);
            model.addAttribute("infoPago", infoPago);

            return "pago/manual";

        } catch (Exception e) {
            log.error("Error al mostrar pago manual: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/mis-pedidos";
        }
    }

    @PostMapping("/manual/confirmar")
    public String confirmarPagoManual(
            @RequestParam Long pedidoId,
            @RequestParam String numeroOperacion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        try {
            Pedido pedido = pedidoService.obtenerPedidoPorId(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (!pedido.getUsuario().getId().equals(usuarioId)) {
                throw new RuntimeException("No tienes permiso");
            }

            String metodoPago = pedido.getMetodoPago().name();
            pagoManualService.registrarPagoManual(pedido, metodoPago, numeroOperacion);

            redirectAttributes.addFlashAttribute("mensaje", "Pago registrado. Te notificaremos cuando sea confirmado.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            return "redirect:/checkout/confirmacion/" + pedido.getId();

        } catch (Exception e) {
            log.error("Error al confirmar pago manual: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/pago/manual/" + pedidoId;
        }
    }
}
