package com.barefoot.service;

import com.barefoot.model.DetallePedido;
import com.barefoot.model.Pedido;
import com.barefoot.model.Producto;
import com.barefoot.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    @Autowired
    private PedidoRepository pedidoRepository;

    // Ventas por mes del año actual
    public Map<String, Double> obtenerVentasPorMes(int year) {
        Map<String, Double> ventasPorMes = new LinkedHashMap<>();

        for (Month month : Month.values()) {
            LocalDateTime inicio = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime fin = inicio.plusMonths(1).minusSeconds(1);

            Double ventas = pedidoRepository.calcularVentasPorFecha(inicio, fin);
            String nombreMes = month.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            ventasPorMes.put(nombreMes, ventas != null ? ventas : 0.0);
        }

        return ventasPorMes;
    }

    // Ventas por año (últimos 5 años)
    public Map<Integer, Double> obtenerVentasPorYear() {
        Map<Integer, Double> ventasPorYear = new LinkedHashMap<>();
        int yearActual = LocalDateTime.now().getYear();

        for (int i = 4; i >= 0; i--) {
            int year = yearActual - i;
            LocalDateTime inicio = LocalDateTime.of(year, 1, 1, 0, 0);
            LocalDateTime fin = LocalDateTime.of(year, 12, 31, 23, 59, 59);

            Double ventas = pedidoRepository.calcularVentasPorFecha(inicio, fin);
            ventasPorYear.put(year, ventas != null ? ventas : 0.0);
        }

        return ventasPorYear;
    }

    // Productos más vendidos
    public List<Map<String, Object>> obtenerProductosMasVendidos(int limite) {
        List<Pedido> pedidosCompletados = pedidoRepository.findAll().stream()
                .filter(p -> p.getEstado() == Pedido.EstadoPedido.ENTREGADO)
                .collect(Collectors.toList());

        Map<Producto, Integer> productosVendidos = new HashMap<>();
        Map<Producto, Double> ingresosPorProducto = new HashMap<>();

        for (Pedido pedido : pedidosCompletados) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto producto = detalle.getProducto();
                productosVendidos.merge(producto, detalle.getCantidad(), Integer::sum);
                ingresosPorProducto.merge(producto, detalle.getSubtotal(), Double::sum);
            }
        }

        return productosVendidos.entrySet().stream()
                .sorted(Map.Entry.<Producto, Integer>comparingByValue().reversed())
                .limit(limite)
                .map(entry -> {
                    Map<String, Object> mapa = new HashMap<>();
                    Producto producto = entry.getKey();
                    mapa.put("id", producto.getId());
                    mapa.put("nombre", producto.getNombre());
                    mapa.put("categoria", producto.getCategoria());
                    mapa.put("cantidadVendida", entry.getValue());
                    mapa.put("ingresos", ingresosPorProducto.get(producto));
                    mapa.put("imagenUrl", producto.getImagenUrl());
                    return mapa;
                })
                .collect(Collectors.toList());
    }

    // Ventas por categoría
    public Map<String, Double> obtenerVentasPorCategoria() {
        List<Pedido> pedidosCompletados = pedidoRepository.findAll().stream()
                .filter(p -> p.getEstado() == Pedido.EstadoPedido.ENTREGADO)
                .collect(Collectors.toList());

        Map<String, Double> ventasPorCategoria = new HashMap<>();

        for (Pedido pedido : pedidosCompletados) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                String categoria = detalle.getProducto().getCategoria();
                ventasPorCategoria.merge(categoria, detalle.getSubtotal(), Double::sum);
            }
        }

        return ventasPorCategoria.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    // Estadísticas de pedidos por estado
    public Map<String, Long> obtenerPedidosPorEstado() {
        List<Pedido> todosPedidos = pedidoRepository.findAll();

        return Arrays.stream(Pedido.EstadoPedido.values())
                .collect(Collectors.toMap(
                        estado -> estado.getNombre(),
                        estado -> todosPedidos.stream()
                                .filter(p -> p.getEstado() == estado)
                                .count(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    // Comparativa mes actual vs mes anterior
    public Map<String, Object> obtenerComparativaVentas() {
        Map<String, Object> comparativa = new HashMap<>();

        LocalDateTime mesActualInicio = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime mesActualFin = LocalDateTime.now();

        LocalDateTime mesAnteriorInicio = mesActualInicio.minusMonths(1);
        LocalDateTime mesAnteriorFin = mesActualInicio.minusSeconds(1);

        Double ventasMesActual = pedidoRepository.calcularVentasPorFecha(mesActualInicio, mesActualFin);
        Double ventasMesAnterior = pedidoRepository.calcularVentasPorFecha(mesAnteriorInicio, mesAnteriorFin);

        ventasMesActual = ventasMesActual != null ? ventasMesActual : 0.0;
        ventasMesAnterior = ventasMesAnterior != null ? ventasMesAnterior : 0.0;

        double diferencia = ventasMesActual - ventasMesAnterior;
        double porcentajeCambio = ventasMesAnterior > 0
                ? (diferencia / ventasMesAnterior) * 100
                : 0.0;

        comparativa.put("mesActual", ventasMesActual);
        comparativa.put("mesAnterior", ventasMesAnterior);
        comparativa.put("diferencia", diferencia);
        comparativa.put("porcentajeCambio", porcentajeCambio);
        comparativa.put("tendencia", diferencia >= 0 ? "positiva" : "negativa");

        return comparativa;
    }

    // Ventas por método de pago
    public Map<String, Object> obtenerVentasPorMetodoPago() {
        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getEstado() != Pedido.EstadoPedido.CANCELADO)
                .filter(p -> p.getMetodoPago() != null)
                .collect(Collectors.toList());

        Map<String, Double> ventasPorMetodo = new HashMap<>();
        Map<String, Long> cantidadPorMetodo = new HashMap<>();

        for (Pedido pedido : pedidos) {
            String metodo = pedido.getMetodoPago().getNombre();
            ventasPorMetodo.merge(metodo, pedido.getTotal(), Double::sum);
            cantidadPorMetodo.merge(metodo, 1L, Long::sum);
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("ventas", ventasPorMetodo);
        resultado.put("cantidad", cantidadPorMetodo);

        return resultado;
    }

    // Resumen general de estadísticas
    public Map<String, Object> obtenerResumenGeneral() {
        Map<String, Object> resumen = new HashMap<>();

        List<Pedido> todosPedidos = pedidoRepository.findAll();
        List<Pedido> pedidosValidos = todosPedidos.stream()
                .filter(p -> p.getEstado() != Pedido.EstadoPedido.CANCELADO)
                .collect(Collectors.toList());

        // Totales generales
        resumen.put("totalPedidos", todosPedidos.size());
        resumen.put("pedidosValidos", pedidosValidos.size());
        resumen.put("pedidosCancelados", todosPedidos.size() - pedidosValidos.size());

        // Ventas totales
        Double ventasTotales = pedidosValidos.stream()
                .mapToDouble(Pedido::getTotal)
                .sum();
        resumen.put("ventasTotales", ventasTotales);

        // Ticket promedio
        Double ticketPromedio = pedidosValidos.isEmpty() ? 0.0
                : ventasTotales / pedidosValidos.size();
        resumen.put("ticketPromedio", ticketPromedio);

        // Total de productos vendidos
        int totalProductosVendidos = pedidosValidos.stream()
                .flatMap(p -> p.getDetalles().stream())
                .mapToInt(DetallePedido::getCantidad)
                .sum();
        resumen.put("totalProductosVendidos", totalProductosVendidos);

        return resumen;
    }

    // Ventas diarias del mes actual
    public Map<Integer, Double> obtenerVentasDiariasDelMes() {
        Map<Integer, Double> ventasDiarias = new LinkedHashMap<>();

        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        int diasEnMes = inicioMes.toLocalDate().lengthOfMonth();

        for (int dia = 1; dia <= diasEnMes; dia++) {
            LocalDateTime inicioDia = inicioMes.withDayOfMonth(dia);
            LocalDateTime finDia = inicioDia.plusDays(1).minusSeconds(1);

            Double ventas = pedidoRepository.calcularVentasPorFecha(inicioDia, finDia);
            ventasDiarias.put(dia, ventas != null ? ventas : 0.0);
        }

        return ventasDiarias;
    }
}