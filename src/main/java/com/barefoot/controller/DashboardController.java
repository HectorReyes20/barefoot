package com.barefoot.controller;

import com.barefoot.model.Producto;
import com.barefoot.service.DashboardService;
import com.barefoot.security.RoleValidator;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String mostrarDashboard(HttpSession session, Model model) {
        // Verificar autenticación
        if (!RoleValidator.estaAutenticado(session)) {
            return "redirect:/login";
        }

        // Si es ENCARGADO, redirigir a dashboard limitado
        if (RoleValidator.esEncargado(session)) {
            return "redirect:/admin/encargado/dashboard";
        }

        // Verificar rol de admin
        if (!RoleValidator.esAdmin(session)) {
            return "redirect:/inicio";
        }

        // Obtener estadísticas generales
        Map<String, Object> estadisticas = dashboardService.obtenerEstadisticasGenerales();
        model.addAttribute("estadisticas", estadisticas);

        // Productos recientes
        List<Producto> productosRecientes = dashboardService.obtenerProductosRecientes(5);
        model.addAttribute("productosRecientes", productosRecientes);

        // Productos con stock bajo
        List<Producto> productosStockBajo = dashboardService.obtenerProductosStockBajo();
        model.addAttribute("productosStockBajo", productosStockBajo);

        // Productos por categoría
        Map<String, Long> productosPorCategoria = dashboardService.obtenerProductosPorCategoria();
        model.addAttribute("productosPorCategoria", productosPorCategoria);

        // Top productos
        List<Map<String, Object>> topProductos = dashboardService.obtenerTopProductos(5);
        model.addAttribute("topProductos", topProductos);

        // Información del usuario
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "admin/dashboard";
    }
}