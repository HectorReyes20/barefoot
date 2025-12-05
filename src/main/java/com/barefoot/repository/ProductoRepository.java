package com.barefoot.repository;
import com.barefoot.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();
    Page<Producto> findByActivoTrue(Pageable pageable);

    List<Producto> findByCategoriaAndActivoTrue(String categoria);
    Page<Producto> findByCategoriaAndActivoTrue(String categoria, Pageable pageable);

    List<Producto> findByDestacadoTrueAndActivoTrue();

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
    Page<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre, Pageable pageable);

    List<Producto> findByPrecioBetweenAndActivoTrue(Double precioMin, Double precioMax);

    List<Producto> findByStockGreaterThanAndActivoTrue(Integer cantidad);

    List<Producto> findByColorAndActivoTrue(String color);

    List<Producto> findByMaterialAndActivoTrue(String material);

    @Query("SELECT p FROM Producto p WHERE " +
            "(:categoria IS NULL OR p.categoria = :categoria) AND " +
            "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
            "(:precioMax IS NULL OR p.precio <= :precioMax) AND " +
            "(:color IS NULL OR p.color = :color) AND " +
            "p.activo = true")
    List<Producto> buscarConFiltros(
            @Param("categoria") String categoria,
            @Param("precioMin") Double precioMin,
            @Param("precioMax") Double precioMax,
            @Param("color") String color
    );

    Long countByCategoria(String categoria);

    List<Producto> findByActivoTrueOrderByPrecioAsc();
    List<Producto> findByActivoTrueOrderByPrecioDesc();

    List<Producto> findTop10ByActivoTrueOrderByFechaCreacionDesc();
}
