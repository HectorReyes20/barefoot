package com.barefoot.controller;

import com.barefoot.service.ReporteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping
    public String mostrarReportes(
            @RequestParam(defaultValue = "0") int year,
            HttpSession session,
            Model model) {

        if (!esAdmin(session)) {
            return "redirect:/login";
        }

        // Año actual por defecto
        if (year == 0) {
            year = LocalDateTime.now().getYear();
        }

        // Datos para gráficos
        model.addAttribute("ventasPorMes", reporteService.obtenerVentasPorMes(year));
        model.addAttribute("ventasPorYear", reporteService.obtenerVentasPorYear());
        model.addAttribute("productosMasVendidos", reporteService.obtenerProductosMasVendidos(10));
        model.addAttribute("ventasPorCategoria", reporteService.obtenerVentasPorCategoria());
        model.addAttribute("pedidosPorEstado", reporteService.obtenerPedidosPorEstado());
        model.addAttribute("comparativaVentas", reporteService.obtenerComparativaVentas());
        model.addAttribute("ventasPorMetodoPago", reporteService.obtenerVentasPorMetodoPago());
        model.addAttribute("resumenGeneral", reporteService.obtenerResumenGeneral());
        model.addAttribute("ventasDiarias", reporteService.obtenerVentasDiariasDelMes());

        // Información adicional
        model.addAttribute("yearSeleccionado", year);
        model.addAttribute("nombreUsuario", session.getAttribute("usuarioNombre"));

        return "admin/reportes/dashboard";
    }

    // API endpoints para AJAX (opcional para gráficos dinámicos)
    @GetMapping("/api/ventas-mes")
    @ResponseBody
    public Map<String, Object> obtenerVentasMesApi(@RequestParam int year) {
        Map<String, Object> response = new HashMap<>();
        response.put("ventas", reporteService.obtenerVentasPorMes(year));
        return response;
    }

    @GetMapping("/api/productos-vendidos")
    @ResponseBody
    public Map<String, Object> obtenerProductosVendidosApi(@RequestParam(defaultValue = "10") int limite) {
        Map<String, Object> response = new HashMap<>();
        response.put("productos", reporteService.obtenerProductosMasVendidos(limite));
        return response;
    }

    @GetMapping("/api/ventas-categoria")
    @ResponseBody
    public Map<String, Object> obtenerVentasCategoriaApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("ventas", reporteService.obtenerVentasPorCategoria());
        return response;
    }

    @GetMapping("/api/resumen")
    @ResponseBody
    public Map<String, Object> obtenerResumenApi() {
        return reporteService.obtenerResumenGeneral();
    }

    private boolean esAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ADMIN".equals(rol);
    }
}