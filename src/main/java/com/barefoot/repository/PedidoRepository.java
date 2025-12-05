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

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.producto WHERE p.id = :id")
    Optional<Pedido> findByIdWithDetallesAndProductos(@Param("id") Long id);

    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    List<Pedido> findByUsuarioOrderByFechaPedidoDesc(Usuario usuario);
    Page<Pedido> findByUsuarioOrderByFechaPedidoDesc(Usuario usuario, Pageable pageable);

    // CORRECCIÓN: Usamos LEFT JOIN FETCH para evitar que desaparezca el pedido si no tiene detalles
    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.detalles WHERE p.usuario = :usuario ORDER BY p.fechaPedido DESC")
    List<Pedido> findByUsuarioWithDetalles(@Param("usuario") Usuario usuario);

    List<Pedido> findByEstadoOrderByFechaPedidoDesc(Pedido.EstadoPedido estado);
    Page<Pedido> findByEstadoOrderByFechaPedidoDesc(Pedido.EstadoPedido estado, Pageable pageable);

    Page<Pedido> findAllByOrderByFechaPedidoDesc(Pageable pageable);

    Long countByEstado(Pedido.EstadoPedido estado);

    List<Pedido> findTop10ByOrderByFechaPedidoDesc();

    List<Pedido> findByFechaPedidoBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.estado != 'CANCELADO'")
    Double calcularTotalVentas();

    @Query("SELECT SUM(p.total) FROM Pedido p WHERE p.estado != 'CANCELADO' AND p.fechaPedido BETWEEN :inicio AND :fin")
    Double calcularVentasPorFecha(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    List<Pedido> findByUsuarioAndEstadoIn(Usuario usuario, List<Pedido.EstadoPedido> estados);

    Optional<Pedido> findByUsuarioAndNumeroPedido(Usuario usuario, String numeroPedido);
    List<Pedido> findByUsuarioId(Long usuarioId);
}