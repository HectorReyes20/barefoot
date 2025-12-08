package com.barefoot.repository;

import com.barefoot.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
    // No necesitas métodos extra por ahora, con los básicos de JPA basta.
}