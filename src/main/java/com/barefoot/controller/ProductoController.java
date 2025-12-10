package com.barefoot.controller;

import com.barefoot.model.Producto;
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
@RequestMapping("/admin/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String listarProductos(HttpSession session, Model model) {
        // Verificar que sea admin
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        List<Producto> productos = productoService.obtenerTodosLosProductos();

        long productosActivos = productos.stream().filter(Producto::getActivo).count();
        long productosDestacados = productos.stream().filter(Producto::getDestacado).count();
        long productosStockBajo = productos.stream().filter(p -> p.getStock() < 10).count();

        model.addAttribute("productos", productos);
        model.addAttribute("productosActivos", productosActivos);
        model.addAttribute("productosDestacados", productosDestacados);
        model.addAttribute("productosStockBajo", productosStockBajo);
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "admin/productos/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("producto", new Producto());
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "admin/productos/formulario";
    }


    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            productoService.crearProducto(producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto creado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/productos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id,
                                          HttpSession session,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        Optional<Producto> producto = productoService.obtenerProductoPorId(id);

        if (producto.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Producto no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/admin/productos";
        }

        model.addAttribute("producto", producto.get());
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));
        model.addAttribute("esEdicion", true);

        return "admin/productos/formulario";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarProducto(@PathVariable Long id,
                                     @ModelAttribute Producto producto,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            productoService.actualizarProducto(id, producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            productoService.eliminarProducto(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/productos";
    }

    @GetMapping("/destacar/{id}")
    public String toggleDestacado(@PathVariable Long id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            productoService.toggleDestacado(id);
            redirectAttributes.addFlashAttribute("mensaje", "Estado destacado actualizado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/productos";
    }

    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Long id,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        Optional<Producto> producto = productoService.obtenerProductoPorId(id);

        if (producto.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Producto no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/admin/productos";
        }

        model.addAttribute("producto", producto.get());
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "admin/productos/detalle";
    }

    // NUEVO MÉTODO: Soft Delete (Desactivar/Reactivar)
    @GetMapping("/desactivar/{id}")
    public String toggleEstadoProducto(@PathVariable Long id,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        // 1. Seguridad
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        try {
            // 2. Buscar producto
            Optional<Producto> optionalProducto = productoService.obtenerProductoPorId(id);

            if (optionalProducto.isPresent()) {
                Producto producto = optionalProducto.get();

                // 3. Invertir el estado (Si es true -> false, Si es false -> true)
                boolean nuevoEstado = !producto.getActivo(); // Asegúrate que tu modelo tenga getActivo() o isActivo()
                producto.setActivo(nuevoEstado);

                // 4. Guardar cambios
                // Usamos actualizar o guardar según tengas en tu servicio
                productoService.actualizarProducto(id, producto);

                // 5. Mensaje de feedback
                String accion = nuevoEstado ? "reactivado" : "desactivado";
                redirectAttributes.addFlashAttribute("mensaje", "Producto " + accion + " correctamente.");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Error: Producto no encontrado.");
                redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al cambiar estado: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/productos";
    }

    private boolean esAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ADMIN".equals(rol);
    }
}