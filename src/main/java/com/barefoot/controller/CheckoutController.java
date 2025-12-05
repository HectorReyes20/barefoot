package com.barefoot.controller;

import com.barefoot.model.*;
import com.barefoot.service.CarritoService;
import com.barefoot.service.PedidoService;
import com.barefoot.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoService productoService;

    // Mostrar página de checkout
    @GetMapping
    public String mostrarCheckout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Verificar que el carrito no esté vacío
        if (carritoService.estaVacio(session)) {
            redirectAttributes.addFlashAttribute("mensaje", "El carrito está vacío");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            return "redirect:/productos";
        }

        // Si no está autenticado, redirigir a autenticación
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/checkout/autenticacion";
        }

        List<ItemCarrito> carrito = carritoService.obtenerCarrito(session);
        Double subtotal = carritoService.calcularTotal(session);
        Double costoEnvio = calcularCostoEnvio(subtotal);
        Double total = subtotal + costoEnvio;

        model.addAttribute("carrito", carrito);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("costoEnvio", costoEnvio);
        model.addAttribute("total", total);
        model.addAttribute("metodosPago", Pedido.MetodoPago.values());

        return "checkout/checkout";
    }

    @PostMapping("/procesar")
    public String procesarPedido(
            @RequestParam String direccionEnvio,
            @RequestParam String metodoPago,
            @RequestParam(required = false) String notas,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            if (session.getAttribute("usuarioId") == null) {
                throw new RuntimeException("Debes iniciar sesión");
            }

            if (carritoService.estaVacio(session)) {
                throw new RuntimeException("El carrito está vacío");
            }

            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                throw new RuntimeException("Usuario no encontrado");
            }

            Pedido pedido = new Pedido();
            pedido.setUsuario(usuario);
            pedido.setDireccionEnvio(direccionEnvio);
            pedido.setMetodoPago(Pedido.MetodoPago.valueOf(metodoPago));
            pedido.setNotas(notas);

            List<ItemCarrito> carrito = carritoService.obtenerCarrito(session);
            Double subtotal = carritoService.calcularTotal(session);
            Double costoEnvio = calcularCostoEnvio(subtotal);

            pedido.setSubtotal(subtotal);
            pedido.setCostoEnvio(costoEnvio);
            pedido.setDescuento(0.0);
            pedido.setTotal(subtotal + costoEnvio);

            List<DetallePedido> detalles = new ArrayList<>();
            for (ItemCarrito item : carrito) {
                Producto producto = productoService.obtenerProductoPorId(item.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getNombre()));

                if (producto.getStock() < item.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
                }

                DetallePedido detalle = new DetallePedido(producto, item.getCantidad(), item.getPersonalizacion());
                detalle.setPedido(pedido);
                detalles.add(detalle);
            }

            pedido.setDetalles(detalles);
            pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);

            Pedido pedidoGuardado = pedidoService.crearPedido(pedido);
            carritoService.vaciarCarrito(session);

            // Redirigir a pago según el método seleccionado
            if ("STRIPE".equals(metodoPago) || "TARJETA_CREDITO".equals(metodoPago)) {
                return "redirect:/pago/procesar/" + pedidoGuardado.getId();
            } else {
                // Para otros métodos de pago, completar directamente
                redirectAttributes.addFlashAttribute("mensaje", "¡Pedido realizado con éxito!");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
                return "redirect:/checkout/confirmacion/" + pedidoGuardado.getId();
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al procesar el pedido: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/checkout";
        }
    }

    // Página de confirmación
    @GetMapping("/confirmacion/{id}")
    public String mostrarConfirmacion(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Verificar autenticación
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        try {
            Pedido pedido = pedidoService.obtenerPedidoPorId(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            // Verificar que el pedido pertenezca al usuario
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (!pedido.getUsuario().getId().equals(usuario.getId())) {
                throw new RuntimeException("No tienes permiso para ver este pedido");
            }

            model.addAttribute("pedido", pedido);

            return "checkout/confirmacion";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/inicio";
        }
    }

    // Página de autenticación en checkout
    @GetMapping("/autenticacion")
    public String mostrarAutenticacion(HttpSession session, Model model) {
        // Verificar que el carrito no esté vacío
        if (carritoService.estaVacio(session)) {
            return "redirect:/productos";
        }

        // Pasar resumen del carrito a la vista
        Double subtotal = carritoService.calcularTotal(session);
        Double costoEnvio = calcularCostoEnvio(subtotal);
        Double total = subtotal + costoEnvio;

        model.addAttribute("subtotal", subtotal);
        model.addAttribute("costoEnvio", costoEnvio);
        model.addAttribute("total", total);
        model.addAttribute("cantidadItems", carritoService.obtenerCarrito(session).size());

        return "checkout/autenticacion";
    }

    // Calcular costo de envío
    private Double calcularCostoEnvio(Double subtotal) {
        // Envío gratis para compras mayores a S/ 400
        if (subtotal >= 400) {
            return 0.0;
        }
        // Costo fijo de envío
        return 15.0;
    }
}