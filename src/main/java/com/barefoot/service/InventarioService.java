package com.barefoot.service;

import com.barefoot.model.*;
import com.barefoot.repository.AlertaInventarioRepository;
import com.barefoot.repository.MovimientoInventarioRepository;
import com.barefoot.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventarioService {

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private AlertaInventarioRepository alertaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // ==================== MOVIMIENTOS ====================

    public MovimientoInventario registrarMovimiento(Producto producto,
                                                    MovimientoInventario.TipoMovimiento tipo,
                                                    Integer cantidad,
                                                    String motivo,
                                                    Usuario usuario) {
        return registrarMovimiento(producto, tipo, cantidad, motivo, null, usuario);
    }

    public MovimientoInventario registrarMovimiento(Producto producto,
                                                    MovimientoInventario.TipoMovimiento tipo,
                                                    Integer cantidad,
                                                    String motivo,
                                                    String numeroDocumento,
                                                    Usuario usuario) {
        // Validaciones
        if (cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        // Crear movimiento
        MovimientoInventario movimiento = new MovimientoInventario(producto, tipo, cantidad, motivo, usuario);
        movimiento.setNumeroDocumento(numeroDocumento);

        // Actualizar stock del producto
        int nuevoStock = movimiento.getStockNuevo();

        if (nuevoStock < 0) {
            throw new RuntimeException("Stock insuficiente. Stock actual: " + producto.getStock());
        }

        producto.setStock(nuevoStock);
        productoRepository.save(producto);

        // Guardar movimiento
        movimiento = movimientoRepository.save(movimiento);

        // Verificar alertas después del movimiento
        verificarYCrearAlertas(producto);

        return movimiento;
    }

    public List<MovimientoInventario> obtenerTodosMovimientos() {
        return movimientoRepository.findAllByOrderByFechaMovimientoDesc();
    }

    public Page<MovimientoInventario> obtenerMovimientosConPaginacion(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaMovimiento").descending());
        return movimientoRepository.findAllByOrderByFechaMovimientoDesc(pageable);
    }

    public List<MovimientoInventario> obtenerMovimientosDeProducto(Producto producto) {
        return movimientoRepository.findByProductoOrderByFechaMovimientoDesc(producto);
    }

    public List<MovimientoInventario> obtenerMovimientosRecientes(int limite) {
        return movimientoRepository.findTop20ByOrderByFechaMovimientoDesc()
                .stream()
                .limit(limite)
                .collect(Collectors.toList());
    }

    public List<MovimientoInventario> obtenerMovimientosDeHoy() {
        return movimientoRepository.findMovimientosDeHoy();
    }

    public List<MovimientoInventario> obtenerMovimientosDelMes() {
        return movimientoRepository.findMovimientosDelMes();
    }

    public List<MovimientoInventario> obtenerMovimientosPorRangoFecha(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository.findByFechaMovimientoBetween(inicio, fin);
    }

    // ==================== ALERTAS ====================

    public void verificarYCrearAlertas(Producto producto) {
        int stock = producto.getStock();

        // Desactivar alertas anteriores del mismo producto
        List<AlertaInventario> alertasAnteriores = alertaRepository
                .findByProductoAndActivaTrueOrderByFechaCreacionDesc(producto);
        alertasAnteriores.forEach(AlertaInventario::desactivar);
        alertaRepository.saveAll(alertasAnteriores);

        // Determinar tipo de alerta según el stock
        if (stock == 0) {
            crearAlerta(producto, AlertaInventario.TipoAlerta.STOCK_AGOTADO,
                    "El producto " + producto.getNombre() + " está AGOTADO", stock);
        } else if (stock <= 3) {
            crearAlerta(producto, AlertaInventario.TipoAlerta.STOCK_CRITICO,
                    "El producto " + producto.getNombre() + " tiene stock CRÍTICO: " + stock + " unidades", stock);
        } else if (stock < 10) {
            crearAlerta(producto, AlertaInventario.TipoAlerta.STOCK_BAJO,
                    "El producto " + producto.getNombre() + " tiene stock bajo: " + stock + " unidades", stock);
        }
    }

    private void crearAlerta(Producto producto, AlertaInventario.TipoAlerta tipo, String mensaje, int stock) {
        // Verificar si ya existe una alerta activa del mismo tipo para este producto
        if (!alertaRepository.existsByProductoAndTipoAndActivaTrue(producto, tipo)) {
            AlertaInventario alerta = new AlertaInventario(producto, tipo, mensaje, stock);
            alertaRepository.save(alerta);
        }
    }

    public List<AlertaInventario> obtenerAlertasActivas() {
        return alertaRepository.findByActivaTrueOrderByFechaCreacionDesc();
    }

    public List<AlertaInventario> obtenerAlertasNoLeidas() {
        return alertaRepository.findByLeidaFalseAndActivaTrueOrderByFechaCreacionDesc();
    }

    public Long contarAlertasNoLeidas() {
        return alertaRepository.countByLeidaFalseAndActivaTrue();
    }

    public List<AlertaInventario> obtenerAlertasCriticas() {
        return alertaRepository.findAlertasCriticas();
    }

    public void marcarAlertaComoLeida(Long alertaId, Usuario usuario) {
        Optional<AlertaInventario> alertaOpt = alertaRepository.findById(alertaId);
        if (alertaOpt.isPresent()) {
            AlertaInventario alerta = alertaOpt.get();
            alerta.marcarComoLeida(usuario);
            alertaRepository.save(alerta);
        }
    }

    public void marcarTodasAlertasComoLeidas(Usuario usuario) {
        List<AlertaInventario> alertas = alertaRepository.findByLeidaFalseAndActivaTrueOrderByFechaCreacionDesc();
        alertas.forEach(a -> a.marcarComoLeida(usuario));
        alertaRepository.saveAll(alertas);
    }

    public void desactivarAlerta(Long alertaId) {
        Optional<AlertaInventario> alertaOpt = alertaRepository.findById(alertaId);
        if (alertaOpt.isPresent()) {
            AlertaInventario alerta = alertaOpt.get();
            alerta.desactivar();
            alertaRepository.save(alerta);
        }
    }

    // ==================== ESTADÍSTICAS ====================

    public Map<String, Object> obtenerEstadisticasInventario() {
        Map<String, Object> stats = new HashMap<>();

        List<Producto> productos = productoRepository.findByActivoTrue();

        // Productos con stock bajo
        long productosStockBajo = productos.stream()
                .filter(p -> p.getStock() < 10)
                .count();

        // Productos sin stock
        long productosSinStock = productos.stream()
                .filter(p -> p.getStock() == 0)
                .count();

        // Valor total del inventario
        double valorInventario = productos.stream()
                .mapToDouble(p -> p.getPrecio() * p.getStock())
                .sum();

        // Total de unidades en stock
        int unidadesTotales = productos.stream()
                .mapToInt(Producto::getStock)
                .sum();

        // Movimientos de hoy
        int movimientosHoy = obtenerMovimientosDeHoy().size();

        // Alertas activas
        long alertasActivas = alertaRepository.countByLeidaFalseAndActivaTrue();

        stats.put("productosStockBajo", productosStockBajo);
        stats.put("productosSinStock", productosSinStock);
        stats.put("valorInventario", valorInventario);
        stats.put("unidadesTotales", unidadesTotales);
        stats.put("movimientosHoy", movimientosHoy);
        stats.put("alertasActivas", alertasActivas);
        stats.put("totalProductos", productos.size());

        return stats;
    }

    public Map<String, Long> obtenerMovimientosPorTipo() {
        return Arrays.stream(MovimientoInventario.TipoMovimiento.values())
                .collect(Collectors.toMap(
                        tipo -> tipo.getNombre(),
                        tipo -> movimientoRepository.countByTipo(tipo),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public List<Map<String, Object>> obtenerProductosStockBajo() {
        return productoRepository.findByActivoTrue().stream()
                .filter(p -> p.getStock() < 10)
                .sorted(Comparator.comparingInt(Producto::getStock))
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("nombre", p.getNombre());
                    map.put("categoria", p.getCategoria());
                    map.put("stock", p.getStock());
                    map.put("precio", p.getPrecio());
                    map.put("imagenUrl", p.getImagenUrl());

                    // Calcular entradas y salidas
                    Integer entradas = movimientoRepository.calcularTotalEntradas(p);
                    Integer salidas = movimientoRepository.calcularTotalSalidas(p);
                    map.put("totalEntradas", entradas != null ? entradas : 0);
                    map.put("totalSalidas", salidas != null ? salidas : 0);

                    return map;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> obtenerEstadisticasProducto(Long productoId) {
        Optional<Producto> productoOpt = productoRepository.findById(productoId);
        if (productoOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Producto producto = productoOpt.get();
        Map<String, Object> stats = new HashMap<>();

        // Información básica
        stats.put("producto", producto);
        stats.put("stockActual", producto.getStock());

        // Movimientos
        List<MovimientoInventario> movimientos = movimientoRepository
                .findByProductoOrderByFechaMovimientoDesc(producto);
        stats.put("totalMovimientos", movimientos.size());

        // Entradas y salidas
        Integer totalEntradas = movimientoRepository.calcularTotalEntradas(producto);
        Integer totalSalidas = movimientoRepository.calcularTotalSalidas(producto);
        stats.put("totalEntradas", totalEntradas != null ? totalEntradas : 0);
        stats.put("totalSalidas", totalSalidas != null ? totalSalidas : 0);

        // Movimientos recientes
        stats.put("movimientosRecientes", movimientos.stream()
                .limit(10)
                .collect(Collectors.toList()));

        // Alertas del producto
        List<AlertaInventario> alertas = alertaRepository
                .findByProductoAndActivaTrueOrderByFechaCreacionDesc(producto);
        stats.put("alertasActivas", alertas);

        return stats;
    }

    // Método auxiliar para verificar todas las alertas del sistema
    public void verificarTodasLasAlertas() {
        List<Producto> productos = productoRepository.findByActivoTrue();
        for (Producto producto : productos) {
            verificarYCrearAlertas(producto);
        }
    }
}