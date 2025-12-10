package com.barefoot.controller;

import com.barefoot.model.ItemCarrito;
import com.barefoot.model.Pedido;
import com.barefoot.model.Usuario;
import com.barefoot.service.CarritoService;
import com.barefoot.service.PedidoService;
import com.barefoot.service.UsuarioService; // Necesitamos esto para guardar la dirección
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
    private UsuarioService usuarioService; // Inyectar esto

    // ------------------ MOSTRAR CHECKOUT ------------------
    @GetMapping
    public String mostrarCheckout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        // 1. Verificar sesión
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Debes iniciar sesión para continuar");
            return "redirect:/login";
        }

        // 2. Verificar carrito vacío
        // Nota: Asegúrate de usar el método correcto según tu CarritoService híbrido
        // Si tu servicio usa "estaVacio(session)", usa ese.
        // Si no, verificamos la lista.
        List<ItemCarrito> carrito = carritoService.obtenerCarrito(session);

        if (carrito.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "El carrito está vacío");
            return "redirect:/productos";
        }

        // 3. Calcular totales
        Double subtotal = carritoService.calcularTotal(session);
        Double costoEnvio = calcularCostoEnvio(subtotal);
        Double total = subtotal + costoEnvio;

        model.addAttribute("carrito", carrito);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("costoEnvio", costoEnvio);
        model.addAttribute("total", total);

        // Pasamos el usuario para rellenar el campo de dirección si ya existe
        Usuario usuario = usuarioService.findById(usuarioId).orElse(new Usuario());
        model.addAttribute("usuario", usuario);

        // Ya no necesitamos pasar "metodosPago" porque la selección se hace en la siguiente pantalla
        return "checkout/checkout";
    }


    // ------------------ PROCESAR DATOS DE ENVÍO ------------------
    // Este método ya NO crea el pedido. Solo guarda la dirección y redirige al pago.
    @PostMapping("/procesar")
    public String procesarDatosEnvio(
            @RequestParam String direccionEnvio,
            @RequestParam(required = false) String notas,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Long usuarioId = (Long) session.getAttribute("usuarioId");
            if (usuarioId == null) return "redirect:/login";

            if (carritoService.estaVacio(session)) {
                return "redirect:/productos";
            }

            // --- KEY FIX: SAVE ADDRESS IN SESSION ---
            // This is the "backpack" logic. We save it here so PagoController can read it later.
            session.setAttribute("direccionEnvio", direccionEnvio);

            // Save notes if present
            if (notas != null && !notas.isEmpty()) {
                session.setAttribute("notasPedido", notas);
            }

            // --- REDIRECT TO PAYMENT SELECTION ---
            return "redirect:/pago/procesar";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al procesar datos: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/checkout";
        }
    }


    // ------------------ CONFIRMACIÓN (Pantalla final) ------------------
    @GetMapping("/confirmacion/{id}")
    public String mostrarConfirmacion(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) return "redirect:/login";

        try {
            Pedido pedido = pedidoService.obtenerPedidoPorId(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            if (!pedido.getUsuario().getId().equals(usuarioId)) {
                return "redirect:/inicio";
            }

            model.addAttribute("pedido", pedido);
            return "checkout/confirmacion";

        } catch (Exception e) {
            return "redirect:/inicio";
        }
    }

    // ------------------ MÉTODO AUXILIAR ------------------
    private Double calcularCostoEnvio(Double subtotal) {
        return (subtotal >= 400) ? 0.0 : 15.0;
    }
}