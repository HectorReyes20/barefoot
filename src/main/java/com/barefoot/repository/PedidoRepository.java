package com.barefoot.repository;

import com.barefoot.model.Pedido;
import com.barefoot.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Buscar por número de pedido
    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    // Buscar pedidos de un usuario
    List<Pedido> findByUsuarioOrderByFechaPedidoDesc(Usuario usuario);
    Page<Pedido> findByUsuarioOrderByFechaPedidoDesc(Usuario usuario, Pageable pageable);

    // Buscar por estado
    List<Pedido> findByEstadoOrderByFechaPedidoDesc(Pedido.EstadoPedido estado);
    Page<Pedido> findByEstadoOrderByFechaPedidoDesc(Pedido.EstadoPedido estado, Pageable pageable);

    // Todos los pedidos ordenados
    Page<Pedido> findAllByOrderByFechaPedidoDesc(Pageable pageable);

    // Contar pedidos por estado
    Long countByEstado(Pedido.EstadoPedido estado);

    // Pedidos recientes
    List<Pedido> findTop10ByOrderByFechaPedidoDesc();

    // Buscar por rango de fechas
    List<Pedido> findByFechaPedidoBetween(LocalDateTime inicio, LocalDateTime fin);

    // Suma total de ventas
    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.estado != 'CANCELADO'")
    Double calcularTotalVentas();

    // Suma de ventas en un rango de fechas
    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.estado != 'CANCELADO' AND p.fechaPedido BETWEEN :inicio AND :fin")
    Double calcularVentasPorFecha(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Pedidos pendientes de un usuario
    List<Pedido> findByUsuarioAndEstadoIn(Usuario usuario, List<Pedido.EstadoPedido> estados);

    // Buscar por usuario y número de pedido
    Optional<Pedido> findByUsuarioAndNumeroPedido(Usuario usuario, String numeroPedido);
    List<Pedido> findByUsuarioId(Long usuarioId);
}