package com.barefoot.repository;

import com.barefoot.model.Pedido;
import com.barefoot.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    Optional<Transaccion> findByReferenciaExterna(String referencia);

    List<Transaccion> findByPedidoId(Long pedidoId);

    List<Transaccion> findByEstado(Transaccion.EstadoTransaccion estado);

    Long countByEstado(Transaccion.EstadoTransaccion estado);

    // Spring Data es más feliz si le pasas el objeto Pedido completo
    Optional<Transaccion> findFirstByPedidoOrderByFechaCreacionDesc(Pedido pedido);

    List<Transaccion> findByReferenciaExternaContainingIgnoreCase(String query);
}

