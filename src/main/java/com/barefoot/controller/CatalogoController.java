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
import java.util.Objects;
import java.util.Collections;

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
        cambie unas cosas del la logica ya que no mostraba, por si occuria algun tipo de error para que sea mas facil de identificar     */
    @GetMapping("/{id}")
    public String verDetalleProducto(@PathVariable Long id,
                                     HttpSession session,
                                     Model model) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);

        if (producto.isEmpty() || !Boolean.TRUE.equals(producto.get().getActivo())) {
            return "redirect:/productos";
        }

        // Obtener productos relacionados (misma categoría) - proteger contra categoría nula
        String categoria = producto.get().getCategoria();
        List<Producto> productosRelacionados;
        if (categoria == null || categoria.trim().isEmpty()) {
            productosRelacionados = Collections.emptyList();
        } else {
            productosRelacionados = productoService.buscarPorCategoria(categoria);
            // Remover el producto actual de la lista (comparación null-safe)
            productosRelacionados.removeIf(p -> Objects.equals(p.getId(), id));
        }

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
        if (categoria == null || categoria.trim().isEmpty()) {
            return "redirect:/productos";
        }

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
    public String buscarProductos(@RequestParam(name = "query", required = false) String query,
                                  HttpSession session,
                                  Model model) {
        List<Producto> productos;

        if (query == null || query.trim().isEmpty()) {
            // Si no viene query, mostramos todos los activos (mejor UX que 400)
            productos = productoService.obtenerProductosActivos();
        } else {
            productos = productoService.buscarPorNombre(query.trim());
        }

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