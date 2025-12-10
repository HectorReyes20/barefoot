package com.barefoot.controller;

import com.barefoot.model.*;
import com.barefoot.repository.PedidoRepository;
import com.barefoot.repository.TransaccionRepository;
import com.barefoot.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/pago")
public class PagoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private PagoManualService pagoManualService;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    // ==========================================
    // 1. PANTALLA DE SELECCIÓN (Usando Carrito)
    // ==========================================
    @GetMapping("/procesar")
    public String mostrarPaginaPago(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";

        // Obtener datos del carrito (No del pedido)
        Usuario usuario = usuarioService.findById(usuarioId).get();
        Carrito carrito = carritoService.obtenerCarrito(usuario);

        if (carrito.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Tu carrito está vacío.");
            return "redirect:/productos";
        }

        // Calculamos total + envío fijo
        double total = carrito.getTotal() + 15.0;

        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);
        model.addAttribute("stripePublicKey", stripePublicKey);

        return "pago/procesar";
    }

    // ==========================================
    // 2. API STRIPE (Adaptada al Carrito)
    // ==========================================
    @PostMapping("/api/iniciar-stripe")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> iniciarPagoStripe(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (usuarioId == null) throw new RuntimeException("No autenticado");

            Usuario usuario = usuarioService.findById(usuarioId).get();
            Carrito carrito = carritoService.obtenerCarrito(usuario);

            // Creamos un pedido TEMPORAL solo para que Stripe calcule el monto
            // Ojo: No lo guardamos en BD todavía
            Pedido pedidoTemp = new Pedido();
            pedidoTemp.setTotal(carrito.getTotal() + 15.0);

            Transaccion transaccion = pagoService.iniciarPagoStripe(pedidoTemp);

            response.put("exito", true);
            response.put("clientSecret", transaccion.getTokenPago());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
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
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            Usuario usuario = usuarioService.findById(usuarioId).get();
            Carrito carrito = carritoService.obtenerCarrito(usuario);

            // Validar con Stripe
            Transaccion transaccion = pagoService.confirmarPago(paymentIntentId);

            if (transaccion.getEstado() == Transaccion.EstadoTransaccion.COMPLETADO) {
                // SI EL PAGO PASÓ -> CREAMOS EL PEDIDO REAL AHORA
                String direccion = (String) session.getAttribute("direccionEnvio");
                if(direccion == null) direccion = "Dirección registrada";

                Pedido nuevoPedido = pedidoService.crearPedidoDesdeCarrito(usuario, carrito, "STRIPE");
                nuevoPedido.setDireccionEnvio(direccion);
                pedidoRepository.save(nuevoPedido);

                // Asignar pedido a la transacción y guardar
                transaccion.setPedido(nuevoPedido);
                transaccionRepository.save(transaccion);

                // Vaciar carrito
                carritoService.vaciarCarrito(usuarioId);

                response.put("exito", true);
                response.put("redirect", "/checkout/confirmacion/" + nuevoPedido.getId());
            } else {
                response.put("error", "Pago no completado");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==========================================
    // 3. PANTALLA YAPE (Usando Carrito)
    // ==========================================
    @GetMapping("/yape")
    public String mostrarPagoYape(HttpSession session, Model model) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";

        Usuario usuario = usuarioService.findById(usuarioId).get();
        Carrito carrito = carritoService.obtenerCarrito(usuario);

        if (carrito.getItems().isEmpty()) return "redirect:/productos";

        double total = carrito.getTotal() + 15.0;
        Map<String, String> infoYape = pagoManualService.obtenerInformacionPago("YAPE");

        model.addAttribute("total", total);
        model.addAttribute("infoYape", infoYape);

        return "pago/yape";
    }

    // ==========================================
    // 4. CONFIRMAR YAPE -> CREAR PEDIDO
    // ==========================================
    @PostMapping("/yape/confirmar")
    public String confirmarPagoYape(
            @RequestParam String codigoAprobacion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";

        try {
            // A. Validar Código (Simulación)
            if (!"654321".equals(codigoAprobacion)) {
                redirectAttributes.addFlashAttribute("mensaje", "Código incorrecto. Intenta de nuevo.");
                redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
                return "redirect:/pago/yape";
            }

            // B. Crear Pedido y Transacción
            Usuario usuario = usuarioService.findById(usuarioId).get();
            Carrito carrito = carritoService.obtenerCarrito(usuario);

            // Recuperar dirección de la sesión (guardada en CheckoutController)
            String direccion = (String) session.getAttribute("direccionEnvio");
            if (direccion == null) direccion = "Dirección por defecto";

            // CREAR PEDIDO CONFIRMADO
            // OJO: Asegúrate de que el método en PedidoService coincida con estos parámetros
            Pedido nuevoPedido = pedidoService.crearPedidoDesdeCarrito(usuario, carrito, "YAPE");
            nuevoPedido.setDireccionEnvio(direccion);
            pedidoRepository.save(nuevoPedido); // Actualizar dirección

            // CREAR TRANSACCIÓN
            Transaccion t = new Transaccion();
            t.setPedido(nuevoPedido);
            t.setMonto(nuevoPedido.getTotal());
            t.setPasarela(Transaccion.Pasarela.YAPE);
            t.setEstado(Transaccion.EstadoTransaccion.COMPLETADO);
            t.setReferenciaExterna(codigoAprobacion);
            t.setFechaConfirmacion(LocalDateTime.now());
            transaccionRepository.save(t);

            // C. Vaciar Carrito
            carritoService.vaciarCarrito(usuario.getId());

            redirectAttributes.addFlashAttribute("mensaje", "¡Pago Exitoso! Pedido generado.");
            return "redirect:/checkout/confirmacion/" + nuevoPedido.getId();

        } catch (Exception e) {
            log.error("Error procesando pago: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            return "redirect:/pago/yape";
        }
    }
}