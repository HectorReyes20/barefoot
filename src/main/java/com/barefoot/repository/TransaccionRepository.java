package com.barefoot.repository;

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

    Optional<Transaccion> findFirstByPedidoIdOrderByFechaCreacionDesc(Long pedidoId);
}

