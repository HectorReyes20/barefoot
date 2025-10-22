package com.barefoot.controller;

import com.barefoot.model.Producto;
import com.barefoot.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
     * Catálogo público de productos con paginación y filtros
     */
    @GetMapping
    public String mostrarCatalogo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String orden,
            HttpSession session,
            Model model) {

        Page<Producto> productosPage;
        String sortBy = "fechaCreacion";
        String direction = "desc";

        // Determinar ordenamiento
        if (orden != null) {
            switch (orden) {
                case "precio-asc":
                    sortBy = "precio";
                    direction = "asc";
                    break;
                case "precio-desc":
                    sortBy = "precio";
                    direction = "desc";
                    break;
                case "nombre":
                    sortBy = "nombre";
                    direction = "asc";
                    break;
            }
        }

        // Aplicar filtros combinados
        if ((categoria != null && !categoria.isEmpty()) ||
                precioMin != null || precioMax != null ||
                (color != null && !color.isEmpty())) {

            // Usar filtros avanzados
            List<Producto> productosFiltrados = productoService.buscarConFiltros(
                    categoria, precioMin, precioMax, color);

            // Aplicar paginación manual
            int start = page * size;
            int end = Math.min(start + size, productosFiltrados.size());
            List<Producto> productosPaginados = productosFiltrados.subList(start, end);

            model.addAttribute("productos", productosPaginados);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", (int) Math.ceil((double) productosFiltrados.size() / size));
            model.addAttribute("totalItems", (long) productosFiltrados.size());
            model.addAttribute("categoriaActual", categoria);
            model.addAttribute("precioMin", precioMin);
            model.addAttribute("precioMax", precioMax);
            model.addAttribute("colorActual", color);
        } else {
            // Sin filtros, usar paginación de base de datos
            productosPage = productoService.buscarProductosConPaginacion(null, page, size, sortBy, direction);
            model.addAttribute("productos", productosPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productosPage.getTotalPages());
            model.addAttribute("totalItems", productosPage.getTotalElements());
        }

        model.addAttribute("pageSize", size);
        model.addAttribute("ordenActual", orden);
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        // Asegurar que esDestacados siempre tenga un valor
        if (!model.containsAttribute("esDestacados")) {
            model.addAttribute("esDestacados", false);
        }

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
     * Buscar productos con paginación
     */
    @GetMapping("/buscar")
    public String buscarProductos(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            HttpSession session,
            Model model) {

        Page<Producto> productosPage = productoService.buscarProductosConPaginacion(
                query, page, size, "fechaCreacion", "desc");

        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productosPage.getTotalPages());
        model.addAttribute("totalItems", productosPage.getTotalElements());
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