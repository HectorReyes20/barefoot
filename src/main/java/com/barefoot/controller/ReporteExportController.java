package com.barefoot.controller;

import com.barefoot.model.Pedido;
import com.barefoot.service.PedidoService;
import com.barefoot.service.ReporteService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reportes/export")
public class ReporteExportController {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private PedidoService pedidoService;

    // Exportar ventas mensuales a CSV
    @GetMapping("/ventas-mes-csv")
    public void exportarVentasMesCSV(
            @RequestParam(defaultValue = "0") int year,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        if (!esAdmin(session)) {
            response.sendRedirect("/login");
            return;
        }

        if (year == 0) {
            year = LocalDateTime.now().getYear();
        }

        Map<String, Double> ventasPorMes = reporteService.obtenerVentasPorMes(year);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"ventas_mensuales_" + year + ".csv\"");

        PrintWriter writer = response.getWriter();

        // Encabezados
        writer.println("Mes,Ventas (S/)");

        // Datos
        ventasPorMes.forEach((mes, ventas) -> {
            writer.println(mes + "," + String.format("%.2f", ventas));
        });

        writer.flush();
    }

    // Exportar productos más vendidos a CSV
    @GetMapping("/productos-vendidos-csv")
    public void exportarProductosVendidosCSV(
            HttpSession session,
            HttpServletResponse response) throws IOException {

        if (!esAdmin(session)) {
            response.sendRedirect("/login");
            return;
        }

        List<Map<String, Object>> productos = reporteService.obtenerProductosMasVendidos(50);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"productos_mas_vendidos.csv\"");

        PrintWriter writer = response.getWriter();

        // Encabezados
        writer.println("Posición,Producto,Categoría,Unidades Vendidas,Ingresos (S/)");

        // Datos
        int posicion = 1;
        for (Map<String, Object> producto : productos) {
            writer.println(
                    posicion++ + "," +
                            "\"" + producto.get("nombre") + "\"," +
                            producto.get("categoria") + "," +
                            producto.get("cantidadVendida") + "," +
                            String.format("%.2f", (Double) producto.get("ingresos"))
            );
        }

        writer.flush();
    }

    // Exportar ventas por categoría a CSV
    @GetMapping("/ventas-categoria-csv")
    public void exportarVentasCategoriaCSV(
            HttpSession session,
            HttpServletResponse response) throws IOException {

        if (!esAdmin(session)) {
            response.sendRedirect("/login");
            return;
        }

        Map<String, Double> ventasPorCategoria = reporteService.obtenerVentasPorCategoria();

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"ventas_por_categoria.csv\"");

        PrintWriter writer = response.getWriter();

        // Encabezados
        writer.println("Categoría,Ventas (S/)");

        // Datos
        ventasPorCategoria.forEach((categoria, ventas) -> {
            writer.println(categoria + "," + String.format("%.2f", ventas));
        });

        writer.flush();
    }

    // Exportar todos los pedidos a CSV
    @GetMapping("/pedidos-csv")
    public void exportarPedidosCSV(
            HttpSession session,
            HttpServletResponse response) throws IOException {

        if (!esAdmin(session)) {
            response.sendRedirect("/login");
            return;
        }

        List<Pedido> pedidos = pedidoService.obtenerTodosPedidos();

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"pedidos_completo.csv\"");

        PrintWriter writer = response.getWriter();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Encabezados
        writer.println("Número Pedido,Cliente,Email,Fecha,Estado,Método Pago,Total (S/)");

        // Datos
        for (Pedido pedido : pedidos) {
            writer.println(
                    pedido.getNumeroPedido() + "," +
                            "\"" + pedido.getUsuario().getNombre() + " " + pedido.getUsuario().getApellido() + "\"," +
                            pedido.getUsuario().getEmail() + "," +
                            pedido.getFechaPedido().format(formatter) + "," +
                            pedido.getEstado().getNombre() + "," +
                            (pedido.getMetodoPago() != null ? pedido.getMetodoPago().getNombre() : "N/A") + "," +
                            String.format("%.2f", pedido.getTotal())
            );
        }

        writer.flush();
    }

    // Exportar resumen general a CSV
    @GetMapping("/resumen-csv")
    public void exportarResumenCSV(
            HttpSession session,
            HttpServletResponse response) throws IOException {

        if (!esAdmin(session)) {
            response.sendRedirect("/login");
            return;
        }

        Map<String, Object> resumen = reporteService.obtenerResumenGeneral();

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"resumen_general.csv\"");

        PrintWriter writer = response.getWriter();

        // Encabezados
        writer.println("Métrica,Valor");

        // Datos
        writer.println("Total Pedidos," + resumen.get("totalPedidos"));
        writer.println("Pedidos Válidos," + resumen.get("pedidosValidos"));
        writer.println("Pedidos Cancelados," + resumen.get("pedidosCancelados"));
        writer.println("Ventas Totales (S/)," + String.format("%.2f", (Double) resumen.get("ventasTotales")));
        writer.println("Ticket Promedio (S/)," + String.format("%.2f", (Double) resumen.get("ticketPromedio")));
        writer.println("Total Productos Vendidos," + resumen.get("totalProductosVendidos"));

        writer.flush();
    }

    private boolean esAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ADMIN".equals(rol);
    }
}