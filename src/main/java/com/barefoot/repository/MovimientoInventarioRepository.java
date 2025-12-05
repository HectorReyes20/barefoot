package com.barefoot.repository;

import com.barefoot.model.MovimientoInventario;
import com.barefoot.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    // Buscar por producto
    List<MovimientoInventario> findByProductoOrderByFechaMovimientoDesc(Producto producto);
    Page<MovimientoInventario> findByProductoOrderByFechaMovimientoDesc(Producto producto, Pageable pageable);

    // Buscar por tipo de movimiento
    List<MovimientoInventario> findByTipoOrderByFechaMovimientoDesc(MovimientoInventario.TipoMovimiento tipo);

    // Todos los movimientos ordenados
    Page<MovimientoInventario> findAllByOrderByFechaMovimientoDesc(Pageable pageable);
    List<MovimientoInventario> findAllByOrderByFechaMovimientoDesc();

    // Movimientos recientes
    List<MovimientoInventario> findTop20ByOrderByFechaMovimientoDesc();

    // Buscar por rango de fechas
    List<MovimientoInventario> findByFechaMovimientoBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar por producto y tipo
    List<MovimientoInventario> findByProductoAndTipo(Producto producto, MovimientoInventario.TipoMovimiento tipo);

    // Contar movimientos por tipo
    Long countByTipo(MovimientoInventario.TipoMovimiento tipo);

    // Buscar por número de documento
    List<MovimientoInventario> findByNumeroDocumento(String numeroDocumento);

    // Movimientos de hoy
    @Query("SELECT m FROM MovimientoInventario m WHERE DATE(m.fechaMovimiento) = CURRENT_DATE ORDER BY m.fechaMovimiento DESC")
    List<MovimientoInventario> findMovimientosDeHoy();

    // Movimientos del mes actual
    @Query("SELECT m FROM MovimientoInventario m WHERE YEAR(m.fechaMovimiento) = YEAR(CURRENT_DATE) " +
            "AND MONTH(m.fechaMovimiento) = MONTH(CURRENT_DATE) ORDER BY m.fechaMovimiento DESC")
    List<MovimientoInventario> findMovimientosDelMes();

    // Total de entradas de un producto
    @Query("SELECT SUM(m.cantidad) FROM MovimientoInventario m WHERE m.producto = :producto " +
            "AND m.tipo IN ('ENTRADA', 'AJUSTE_ENTRADA', 'DEVOLUCION')")
    Integer calcularTotalEntradas(@Param("producto") Producto producto);

    // Total de salidas de un producto
    @Query("SELECT SUM(m.cantidad) FROM MovimientoInventario m WHERE m.producto = :producto " +
            "AND m.tipo IN ('SALIDA', 'AJUSTE_SALIDA', 'MERMA')")
    Integer calcularTotalSalidas(@Param("producto") Producto producto);
}