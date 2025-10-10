package com.barefoot.controller;

import com.barefoot.model.Producto;
import com.barefoot.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/productos")
public class CatalogoController {

    @Autowired
    private ProductoService productoService;

    /**
     * Catálogo público de productos
     */
    @GetMapping
    public String mostrarCatalogo(HttpSession session, Model model) {
        List<Producto> productos = productoService.obtenerProductosActivos();

        model.addAttribute("productos", productos);
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "productos/catalogo";
    }

    /**
     * Ver detalle de un producto
     */
    @GetMapping("/{id}")
    public String verDetalleProducto(@PathVariable Long id,
                                     HttpSession session,
                                     Model model) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);

        if (producto.isEmpty() || !producto.get().getActivo()) {
            return "redirect:/productos";
        }

        // Obtener productos relacionados (misma categoría)
        List<Producto> productosRelacionados = productoService
                .buscarPorCategoria(producto.get().getCategoria());

        // Remover el producto actual de la lista
        productosRelacionados.removeIf(p -> p.getId().equals(id));

        model.addAttribute("producto", producto.get());
        model.addAttribute("productosRelacionados", productosRelacionados);
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "productos/detalle";
    }

    /**
     * Filtrar por categoría
     */
    @GetMapping("/categoria/{categoria}")
    public String filtrarPorCategoria(@PathVariable String categoria,
                                      HttpSession session,
                                      Model model) {
        List<Producto> productos = productoService.buscarPorCategoria(categoria);

        model.addAttribute("productos", productos);
        model.addAttribute("categoriaActual", categoria);
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "productos/catalogo";
    }

    /**
     * Buscar productos
     */
    @GetMapping("/buscar")
    public String buscarProductos(@RequestParam String query,
                                  HttpSession session,
                                  Model model) {
        List<Producto> productos = productoService.buscarPorNombre(query);

        model.addAttribute("productos", productos);
        model.addAttribute("query", query);
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "productos/catalogo";
    }

    /**
     * Productos destacados
     */
    @GetMapping("/destacados")
    public String productosDestacados(HttpSession session, Model model) {
        List<Producto> productos = productoService.obtenerProductosDestacados();

        model.addAttribute("productos", productos);
        model.addAttribute("esDestacados", true);
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "productos/catalogo";
    }
}