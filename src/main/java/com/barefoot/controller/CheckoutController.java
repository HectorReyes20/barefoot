package com.barefoot.controller;

import com.barefoot.model.ItemCarrito;
import com.barefoot.model.Pedido;
import com.barefoot.model.Usuario;
import com.barefoot.service.CarritoService;
import com.barefoot.service.PedidoService;
import com.barefoot.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UsuarioService usuarioService;

    // ------------------ 1. MOSTRAR PANTALLA DE ENVÍO ------------------
    @GetMapping
    public String mostrarCheckout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        // 🔒 SEGURIDAD: Verificar sesión
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Debes iniciar sesión para finalizar la compra.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");

            // --- NUEVO: GUARDAR INTENCIÓN PARA VOLVER AUTOMÁTICAMENTE ---
            session.setAttribute("redirect", "checkout");

            return "redirect:/login";
        }

        // 2. Verificar carrito vacío
        if (carritoService.estaVacio(session)) {
            redirectAttributes.addFlashAttribute("mensaje", "El carrito está vacío");
            redirectAttributes.addFlashAttribute("tipoMensaje", "info");
            return "redirect:/productos";
        }

        // 3. Calcular totales
        List<ItemCarrito> carrito = carritoService.obtenerCarrito(session);
        Double subtotal = carritoService.calcularTotal(session);
        Double costoEnvio = calcularCostoEnvio(subtotal);
        Double total = subtotal + costoEnvio;

        model.addAttribute("carrito", carrito);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("costoEnvio", costoEnvio);
        model.addAttribute("total", total);

        // 4. Pre-llenar datos del usuario
        Usuario usuario = usuarioService.findById(usuarioId).orElse(new Usuario());
        model.addAttribute("usuario", usuario);

        return "checkout/checkout";
    }


    // ------------------ 2. PROCESAR DATOS Y PASAR A PAGO ------------------
    @PostMapping("/procesar")
    public String procesarDatosEnvio(
            @RequestParam String direccionEnvio,
            @RequestParam(required = false) String notas,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 🔒 SEGURIDAD
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        try {
            if (carritoService.estaVacio(session)) {
                return "redirect:/productos";
            }

            // GUARDAR DATOS EN SESIÓN (MOCHILA) PARA EL SIGUIENTE PASO
            session.setAttribute("direccionEnvio", direccionEnvio);

            if (notas != null && !notas.isEmpty()) {
                session.setAttribute("notasPedido", notas);
            }

            // Redirigir al controlador de pagos
            return "redirect:/pago/procesar";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al procesar datos: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/checkout";
        }
    }


    // ------------------ 3. CONFIRMACIÓN FINAL ------------------
    @GetMapping("/confirmacion/{id}")
    public String mostrarConfirmacion(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        // 🔒 SEGURIDAD
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";

        try {
            Pedido pedido = pedidoService.obtenerPedidoPorId(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            // Verificar que el pedido pertenezca al usuario logueado (Seguridad Extra)
            if (!pedido.getUsuario().getId().equals(usuarioId)) {
                return "redirect:/inicio";
            }

            model.addAttribute("pedido", pedido);
            return "checkout/confirmacion";

        } catch (Exception e) {
            return "redirect:/inicio";
        }
    }

    private Double calcularCostoEnvio(Double subtotal) {
        return (subtotal >= 400) ? 0.0 : 15.0;
    }
}