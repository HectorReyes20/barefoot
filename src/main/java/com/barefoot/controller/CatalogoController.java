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

        if ((categoria != null && !categoria.isEmpty()) ||
                precioMin != null || precioMax != null ||
                (color != null && !color.isEmpty())) {

            List<Producto> productosFiltrados = productoService.buscarConFiltros(
                    categoria, precioMin, precioMax, color);


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

            productosPage = productoService.buscarProductosConPaginacion(null, page, size, sortBy, direction);
            model.addAttribute("productos", productosPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productosPage.getTotalPages());
            model.addAttribute("totalItems", productosPage.getTotalElements());
        }

        model.addAttribute("pageSize", size);
        model.addAttribute("ordenActual", orden);


        if (!model.containsAttribute("esDestacados")) {
            model.addAttribute("esDestacados", false);
        }

        return "productos/catalogo";
    }

    @GetMapping("/{id}")
    public String verDetalleProducto(@PathVariable Long id,
                                     HttpSession session,
                                     Model model) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);

        if (producto.isEmpty() || !producto.get().getActivo()) {
            return "redirect:/productos";
        }

        List<Producto> productosRelacionados = productoService
                .buscarPorCategoria(producto.get().getCategoria());

        productosRelacionados.removeIf(p -> p.getId().equals(id));

        model.addAttribute("producto", producto.get());
        model.addAttribute("productosRelacionados", productosRelacionados);


        return "productos/detalle";
    }

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


        return "productos/catalogo";
    }


    @GetMapping("/categoria/{nombre}")
    public String filtrarPorCategoria(
            @PathVariable String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String orden,
            HttpSession session,
            Model model) {

        String sortBy = "fechaCreacion";
        String direction = "desc";

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

        List<Producto> productosFiltrados = productoService.buscarPorCategoria(nombre);

        int start = page * size;
        int end = Math.min(start + size, productosFiltrados.size());
        List<Producto> productosPaginados = productosFiltrados.subList(start, end);

        model.addAttribute("productos", productosPaginados);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) productosFiltrados.size() / size));
        model.addAttribute("totalItems", (long) productosFiltrados.size());
        model.addAttribute("categoriaActual", nombre);
        model.addAttribute("pageSize", size);
        model.addAttribute("ordenActual", orden);
        model.addAttribute("esDestacados", false);

        return "productos/catalogo";
    }

    @GetMapping("/destacados")
    public String productosDestacados(HttpSession session, Model model) {
        List<Producto> productos = productoService.obtenerProductosDestacados();

        model.addAttribute("productos", productos);
        model.addAttribute("esDestacados", true);


        return "productos/catalogo";
    }
}