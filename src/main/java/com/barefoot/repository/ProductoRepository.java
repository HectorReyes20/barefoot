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

    // Buscar productos activos
    List<Producto> findByActivoTrue();
    Page<Producto> findByActivoTrue(Pageable pageable);

    // Buscar por categoría
    List<Producto> findByCategoriaAndActivoTrue(String categoria);
    Page<Producto> findByCategoriaAndActivoTrue(String categoria, Pageable pageable);

    // Buscar productos destacados
    List<Producto> findByDestacadoTrueAndActivoTrue();

    // Buscar por nombre (búsqueda parcial, insensible a mayúsculas)
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
    Page<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre, Pageable pageable);

    // Buscar por rango de precio
    List<Producto> findByPrecioBetweenAndActivoTrue(Double precioMin, Double precioMax);

    // Buscar productos con stock disponible
    List<Producto> findByStockGreaterThanAndActivoTrue(Integer cantidad);

    // Buscar por color
    List<Producto> findByColorAndActivoTrue(String color);

    // Buscar por material
    List<Producto> findByMaterialAndActivoTrue(String material);

    // Buscar por múltiples criterios (Query personalizada)
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

    // Contar productos por categoría
    Long countByCategoria(String categoria);

    // Obtener productos ordenados por precio
    List<Producto> findByActivoTrueOrderByPrecioAsc();
    List<Producto> findByActivoTrueOrderByPrecioDesc();

    // Obtener productos más recientes
    List<Producto> findTop10ByActivoTrueOrderByFechaCreacionDesc();
}
