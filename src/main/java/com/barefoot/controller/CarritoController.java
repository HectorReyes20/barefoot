package com.barefoot.controller;

import com.barefoot.model.ItemCarrito;
import com.barefoot.model.Producto;
import com.barefoot.service.CarritoService;
import com.barefoot.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private ProductoService productoService;

    // Ver el carrito
    @GetMapping
    public String verCarrito(HttpSession session, Model model) {
        List<ItemCarrito> carrito = carritoService.obtenerCarrito(session);
        Double total = carritoService.calcularTotal(session);

        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);
        model.addAttribute("cantidadItems", carrito.size());

        return "carrito/ver";
    }

    // Agregar producto al carrito
    @PostMapping("/agregar")
    public String agregarAlCarrito(
            @RequestParam Long productoId,
            @RequestParam(defaultValue = "1") Integer cantidad,
            @RequestParam(required = false) String personalizacion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. VALIDACIÓN: Si no está logueado, guardar datos y mandar a login
        if (session.getAttribute("usuarioId") == null) {
            session.setAttribute("pendiente_prod_id", productoId);
            session.setAttribute("pendiente_cant", cantidad);
            session.setAttribute("pendiente_pers", personalizacion);

            redirectAttributes.addFlashAttribute("mensaje", "Inicia sesión para agregar el producto en el carrito.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "info");
            return "redirect:/login"; // Redirige al login
        }

        // 2. Flujo normal (Usuario logueado)
        try {
            Optional<Producto> productoOpt = productoService.obtenerProductoPorId(productoId);
            if (productoOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");

            carritoService.agregarProducto(session, productoOpt.get(), cantidad, personalizacion);

            redirectAttributes.addFlashAttribute("mensaje", "Producto agregado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }
        return "redirect:/carrito";
    }

    // 3. NUEVO ENDPOINT: Procesa lo que estaba pendiente tras el login
    @GetMapping("/procesar-pendiente")
    public String procesarPendiente(HttpSession session, RedirectAttributes redirectAttributes) {
        Long prodId = (Long) session.getAttribute("pendiente_prod_id");

        if (prodId != null) {
            Integer cant = (Integer) session.getAttribute("pendiente_cant");
            String pers = (String) session.getAttribute("pendiente_pers");

            try {
                Optional<Producto> productoOpt = productoService.obtenerProductoPorId(prodId);
                if (productoOpt.isPresent()) {
                    carritoService.agregarProducto(session, productoOpt.get(), cant, pers);
                    redirectAttributes.addFlashAttribute("mensaje", "¡Tu diseño ha sido guardado en el carrito!");
                    redirectAttributes.addFlashAttribute("tipoMensaje", "success");
                }
            } catch (Exception e) {
                // Log error
            }
            // Limpiar sesión
            session.removeAttribute("pendiente_prod_id");
            session.removeAttribute("pendiente_cant");
            session.removeAttribute("pendiente_pers");
        }
        return "redirect:/carrito";
    }

    // Actualizar cantidad
    @PostMapping("/actualizar/{productoId}")
    public String actualizarCantidad(
            @PathVariable Long productoId,
            @RequestParam Integer cantidad,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Producto> productoOpt = productoService.obtenerProductoPorId(productoId);
            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                if (producto.getStock() < cantidad) {
                    throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStock());
                }
            }

            carritoService.actualizarCantidad(session, productoId, cantidad);

            redirectAttributes.addFlashAttribute("mensaje", "Cantidad actualizada");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/carrito";
    }

    // Eliminar producto del carrito
    @PostMapping("/eliminar/{productoId}")
    public String eliminarProducto(
            @PathVariable Long productoId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        carritoService.eliminarProducto(session, productoId);

        redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito");
        redirectAttributes.addFlashAttribute("tipoMensaje", "info");

        return "redirect:/carrito";
    }

    // Vaciar el carrito
    @PostMapping("/vaciar")
    public String vaciarCarrito(HttpSession session, RedirectAttributes redirectAttributes) {
        carritoService.vaciarCarrito(session);

        redirectAttributes.addFlashAttribute("mensaje", "Carrito vaciado");
        redirectAttributes.addFlashAttribute("tipoMensaje", "info");

        return "redirect:/carrito";
    }

    // Proceder al checkout
    @GetMapping("/checkout")
    public String irACheckout(HttpSession session, RedirectAttributes redirectAttributes) {

        // Verificar que el usuario esté logueado
        if (session.getAttribute("usuarioId") == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Debes iniciar sesión para continuar");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            session.setAttribute("redirectAfterLogin", "/carrito/checkout");
            return "redirect:/login";
        }

        // Verificar que el carrito no esté vacío
        if (carritoService.estaVacio(session)) {
            redirectAttributes.addFlashAttribute("mensaje", "El carrito está vacío");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            return "redirect:/productos";
        }

        return "redirect:/checkout";
    }
}
