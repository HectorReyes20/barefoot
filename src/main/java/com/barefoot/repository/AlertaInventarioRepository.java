package com.barefoot.repository;

import com.barefoot.model.AlertaInventario;
import com.barefoot.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertaInventarioRepository extends JpaRepository<AlertaInventario, Long> {

    // Alertas activas
    List<AlertaInventario> findByActivaTrueOrderByFechaCreacionDesc();
    Page<AlertaInventario> findByActivaTrueOrderByFechaCreacionDesc(Pageable pageable);

    // Alertas no leídas
    List<AlertaInventario> findByLeidaFalseAndActivaTrueOrderByFechaCreacionDesc();
    Long countByLeidaFalseAndActivaTrue();

    // Alertas por tipo
    List<AlertaInventario> findByTipoAndActivaTrueOrderByFechaCreacionDesc(AlertaInventario.TipoAlerta tipo);

    // Alertas por producto
    List<AlertaInventario> findByProductoAndActivaTrueOrderByFechaCreacionDesc(Producto producto);
    Optional<AlertaInventario> findFirstByProductoAndTipoAndActivaTrueOrderByFechaCreacionDesc(
            Producto producto, AlertaInventario.TipoAlerta tipo);

    // Todas las alertas ordenadas
    Page<AlertaInventario> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    // Alertas críticas activas
    @Query("SELECT a FROM AlertaInventario a WHERE a.activa = true AND a.leida = false " +
            "AND a.tipo IN ('STOCK_CRITICO', 'STOCK_AGOTADO') ORDER BY a.fechaCreacion DESC")
    List<AlertaInventario> findAlertasCriticas();

    // Contar alertas por tipo
    Long countByTipoAndActivaTrue(AlertaInventario.TipoAlerta tipo);

    // Verificar si existe alerta activa para un producto
    boolean existsByProductoAndTipoAndActivaTrue(Producto producto, AlertaInventario.TipoAlerta tipo);
}