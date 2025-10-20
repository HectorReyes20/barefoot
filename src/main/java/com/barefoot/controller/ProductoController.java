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

    /**
     * Listar todos los productos (Admin)
     */
    @GetMapping
    public String listarProductos(HttpSession session, Model model) {
        // Verificar que sea admin
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        List<Producto> productos = productoService.obtenerTodosLosProductos();

        // Calcular estadísticas
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

    /**
     * Mostrar formulario para crear producto
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("producto", new Producto());
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "admin/productos/formulario";
    }

    /**
     * Guardar nuevo producto
     */
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

    /**
     * Mostrar formulario para editar producto
     */
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

    /**
     * Actualizar producto
     */
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

    /**
     * Eliminar producto (soft delete)
     */
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

    /**
     * Toggle destacado
     */
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

    /**
     * Ver detalle de producto
     */
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

    // Método auxiliar para verificar si es admin
    private boolean esAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ADMIN".equals(rol);
    }
}