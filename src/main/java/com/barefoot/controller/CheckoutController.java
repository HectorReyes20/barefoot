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


    // ------------------ MOSTRAR CHECKOUT ------------------
    @GetMapping
    public String mostrarCheckout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        // Verificar autenticación
        if (session.getAttribute("usuarioId") == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Debes iniciar sesión para continuar");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            return "redirect:/login";
        }

        // Carrito vacío
        if (carritoService.estaVacio(session)) {
            redirectAttributes.addFlashAttribute("mensaje", "El carrito está vacío");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            return "redirect:/productos";
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


    // ------------------ PROCESAR PEDIDO (ACTUALIZADO) ------------------
    @PostMapping("/procesar")
    public String procesarPedido(
            @RequestParam String direccionEnvio,
            @RequestParam String metodoPago,
            @RequestParam(required = false) String notas,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // 1. Validaciones básicas
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

            // 2. Crear objeto Pedido base
            Pedido pedido = new Pedido();
            pedido.setUsuario(usuario);
            pedido.setDireccionEnvio(direccionEnvio);
            Pedido.MetodoPago metodo = Pedido.MetodoPago.valueOf(metodoPago);
            pedido.setMetodoPago(metodo);
            pedido.setNotas(notas);

            // 3. Cálculos de Totales
            List<ItemCarrito> carrito = carritoService.obtenerCarrito(session);
            Double subtotal = carritoService.calcularTotal(session);
            Double costoEnvio = calcularCostoEnvio(subtotal);

            pedido.setSubtotal(subtotal);
            pedido.setCostoEnvio(costoEnvio);
            pedido.setDescuento(0.0);
            pedido.setTotal(subtotal + costoEnvio);
            // La línea pedido.setEstado(Pedido.EstadoPedido.PENDIENTE); ha sido eliminada.
            // El estado por defecto ahora es CONFIRMADO, establecido en el modelo Pedido.

            // 4. Crear Detalles y Verificar Stock
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

            // 5. Guardar Pedido en BD
            Pedido pedidoGuardado = pedidoService.crearPedido(pedido);

            // 6. Vaciar carrito
            carritoService.vaciarCarrito(session);

            // 7. Redirigir según el método de pago
            if (metodo.isRequierePasarela()) {
                return "redirect:/pago/procesar/" + pedidoGuardado.getId();
            }

            return "redirect:/pago/manual/" + pedidoGuardado.getId();


        } catch (Exception e) {
            // Manejo de errores
            redirectAttributes.addFlashAttribute("mensaje", "Error al procesar el pedido: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/checkout";
        }
    }


    // ------------------ CONFIRMACIÓN ------------------
    @GetMapping("/confirmacion/{id}")
    public String mostrarConfirmacion(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        try {
            Pedido pedido = pedidoService.obtenerPedidoPorId(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

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


    // ------------------ AUTENTICACIÓN EN CHECKOUT ------------------
    @GetMapping("/autenticacion")
    public String mostrarAutenticacion(HttpSession session, Model model) {

        if (carritoService.estaVacio(session)) {
            return "redirect:/productos";
        }

        Double subtotal = carritoService.calcularTotal(session);
        Double costoEnvio = calcularCostoEnvio(subtotal);
        Double total = subtotal + costoEnvio;

        model.addAttribute("subtotal", subtotal);
        model.addAttribute("costoEnvio", costoEnvio);
        model.addAttribute("total", total);
        model.addAttribute("cantidadItems", carritoService.obtenerCarrito(session).size());

        return "checkout/autenticacion";
    }


    // ------------------ MÉTODO AUXILIAR: COSTO ENVÍO ------------------
    private Double calcularCostoEnvio(Double subtotal) {
        if (subtotal >= 400) return 0.0;
        return 15.0;
    }

}
