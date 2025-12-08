package com.barefoot.service;

import com.barefoot.model.MovimientoInventario;
import com.barefoot.model.Producto;
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
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // --- NUEVO: NECESARIO PARA GUARDAR EL HISTORIAL ---
    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    public Producto crearProducto(Producto producto) {
        if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
            throw new RuntimeException("El precio debe ser mayor a 0");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del producto es obligatorio");
        }

        // Guardar producto
        Producto nuevo = productoRepository.save(producto);

        // Opcional: Registrar movimiento de "Stock Inicial" al crear
        if (nuevo.getStock() > 0) {
            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(nuevo);
            mov.setTipo(MovimientoInventario.TipoMovimiento.ENTRADA);
            mov.setCantidad(nuevo.getStock());
            mov.setStockAnterior(0);
            mov.setStockNuevo(nuevo.getStock());
            mov.setFechaMovimiento(LocalDateTime.now());
            mov.setMotivo("Stock Inicial - Creación de producto");
            movimientoRepository.save(mov);
        }

        return nuevo;
    }

    public Producto actualizarProducto(Long id, Producto productoActualizado) {
        Optional<Producto> productoExistente = productoRepository.findById(id);
        if (productoExistente.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }

        Producto producto = productoExistente.get();

        // Detectar si hubo cambio manual de stock desde el editor
        int stockAnterior = producto.getStock();
        int stockNuevo = productoActualizado.getStock();

        producto.setNombre(productoActualizado.getNombre());
        producto.setDescripcion(productoActualizado.getDescripcion());
        producto.setPrecio(productoActualizado.getPrecio());
        producto.setPrecioDescuento(productoActualizado.getPrecioDescuento());
        producto.setImagenUrl(productoActualizado.getImagenUrl());
        producto.setCategoria(productoActualizado.getCategoria());
        producto.setStock(stockNuevo); // Actualizamos valor
        producto.setTalla(productoActualizado.getTalla());
        producto.setColor(productoActualizado.getColor());
        producto.setMaterial(productoActualizado.getMaterial());
        producto.setActivo(productoActualizado.getActivo());
        producto.setDestacado(productoActualizado.getDestacado());

        Producto guardado = productoRepository.save(producto);

        // Si el stock cambió al editar, registrar el movimiento
        if (stockAnterior != stockNuevo) {
            registrarMovimientoCambioStock(guardado, stockAnterior, stockNuevo, "Actualización desde edición de producto");
        }

        return guardado;
    }

    public void eliminarProducto(Long id) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isEmpty()) throw new RuntimeException("Producto no encontrado con ID: " + id);
        Producto prod = producto.get();
        prod.setActivo(false);
        productoRepository.save(prod);
    }

    public void eliminarProductoPermanente(Long id) {
        productoRepository.deleteById(id);
    }

    public List<Producto> obtenerTodosLosProductos() { return productoRepository.findAll(); }
    public List<Producto> obtenerProductosActivos() { return productoRepository.findByActivoTrue(); }
    public Optional<Producto> obtenerProductoPorId(Long id) { return productoRepository.findById(id); }

    // ... (Tus métodos de búsqueda se mantienen igual) ...
    public List<Producto> buscarPorCategoria(String categoria) { return productoRepository.findByCategoriaAndActivoTrue(categoria); }
    public List<Producto> obtenerProductosDestacados() { return productoRepository.findByDestacadoTrueAndActivoTrue(); }
    public List<Producto> buscarPorNombre(String nombre) { return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre); }
    public List<Producto> buscarPorRangoPrecio(Double precioMin, Double precioMax) { return productoRepository.findByPrecioBetweenAndActivoTrue(precioMin, precioMax); }

    public Producto aplicarDescuento(Long id, Double porcentaje) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Producto producto = productoOpt.get();
        Double precioOriginal = producto.getPrecio();
        Double descuento = precioOriginal * (porcentaje / 100);
        producto.setPrecioDescuento(precioOriginal - descuento);
        return productoRepository.save(producto);
    }

    public Producto quitarDescuento(Long id) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Producto producto = productoOpt.get();
        producto.setPrecioDescuento(null);
        return productoRepository.save(producto);
    }

    // --- MÉTODO ACTUALIZADO: AJUSTE MANUAL DE STOCK ---
    public Producto actualizarStock(Long id, Integer nuevoStock) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");

        Producto producto = productoOpt.get();
        int stockAnterior = producto.getStock();

        // Actualizamos el producto
        producto.setStock(nuevoStock);
        Producto guardado = productoRepository.save(producto);

        // Registramos el movimiento en el historial
        if (stockAnterior != nuevoStock) {
            registrarMovimientoCambioStock(guardado, stockAnterior, nuevoStock, "Ajuste manual de inventario");
        }

        return guardado;
    }

    // --- MÉTODO ACTUALIZADO: VENTA (REDUCIR STOCK) ---
    public Producto reducirStock(Long id, Integer cantidad) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");

        Producto producto = productoOpt.get();

        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        int stockAnterior = producto.getStock();
        int nuevoStock = stockAnterior - cantidad;

        // Actualizar producto
        producto.setStock(nuevoStock);
        Producto guardado = productoRepository.save(producto);

        // --- REGISTRAR MOVIMIENTO DE SALIDA (VENTA) ---
        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(guardado);
        mov.setTipo(MovimientoInventario.TipoMovimiento.SALIDA);
        mov.setCantidad(cantidad);
        mov.setStockAnterior(stockAnterior);
        mov.setStockNuevo(nuevoStock);
        mov.setFechaMovimiento(LocalDateTime.now());
        mov.setMotivo("Venta realizada"); // O "Venta Online"
        movimientoRepository.save(mov);

        return guardado;
    }

    public Producto toggleDestacado(Long id) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isEmpty()) throw new RuntimeException("Producto no encontrado");
        Producto producto = productoOpt.get();
        producto.setDestacado(!producto.getDestacado());
        return productoRepository.save(producto);
    }

    // ... (El resto de métodos de búsqueda y paginación se mantienen igual) ...
    public List<Producto> buscarConFiltros(String categoria, Double precioMin, Double precioMax, String color) {
        return productoRepository.buscarConFiltros(categoria, precioMin, precioMax, color);
    }
    public Page<Producto> obtenerProductosConPaginacion(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        return productoRepository.findAll(pageable);
    }
    public Page<Producto> obtenerProductosActivosConPaginacion(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
        return productoRepository.findByActivoTrue(pageable);
    }
    public Page<Producto> obtenerProductosPorCategoriaConPaginacion(String categoria, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productoRepository.findByCategoriaAndActivoTrue(categoria, pageable);
    }
    public Page<Producto> buscarProductosConPaginacion(String query, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (query != null && !query.trim().isEmpty()) {
            return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(query, pageable);
        }
        return productoRepository.findByActivoTrue(pageable);
    }

    // --- Helper privado para no repetir código al guardar movimientos ---
    private void registrarMovimientoCambioStock(Producto producto, int anterior, int nuevo, String motivo) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(producto);
        mov.setStockAnterior(anterior);
        mov.setStockNuevo(nuevo);
        mov.setFechaMovimiento(LocalDateTime.now());
        mov.setMotivo(motivo);

        int diferencia = nuevo - anterior;
        mov.setCantidad(Math.abs(diferencia));

        if (diferencia > 0) {
            mov.setTipo(MovimientoInventario.TipoMovimiento.AJUSTE_ENTRADA);
        } else {
            mov.setTipo(MovimientoInventario.TipoMovimiento.AJUSTE_SALIDA);
        }
        movimientoRepository.save(mov);
    }
}