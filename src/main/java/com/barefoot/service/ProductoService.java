package com.barefoot.service;

import com.barefoot.model.Producto;
import com.barefoot.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

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

        return productoRepository.save(producto);
    }

    public Producto actualizarProducto(Long id, Producto productoActualizado) {
        Optional<Producto> productoExistente = productoRepository.findById(id);

        if (productoExistente.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }

        Producto producto = productoExistente.get();

        producto.setNombre(productoActualizado.getNombre());
        producto.setDescripcion(productoActualizado.getDescripcion());
        producto.setPrecio(productoActualizado.getPrecio());
        producto.setPrecioDescuento(productoActualizado.getPrecioDescuento());
        producto.setImagenUrl(productoActualizado.getImagenUrl());
        producto.setCategoria(productoActualizado.getCategoria());
        producto.setStock(productoActualizado.getStock());
        producto.setTalla(productoActualizado.getTalla());
        producto.setColor(productoActualizado.getColor());
        producto.setMaterial(productoActualizado.getMaterial());
        producto.setActivo(productoActualizado.getActivo());
        producto.setDestacado(productoActualizado.getDestacado());

        return productoRepository.save(producto);
    }

    public void eliminarProducto(Long id) {
        Optional<Producto> producto = productoRepository.findById(id);

        if (producto.isEmpty()) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }

        Producto prod = producto.get();
        prod.setActivo(false);
        productoRepository.save(prod);
    }

    public void eliminarProductoPermanente(Long id) {
        productoRepository.deleteById(id);
    }

    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerProductosActivos() {
        return productoRepository.findByActivoTrue();
    }

    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    public List<Producto> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaAndActivoTrue(categoria);
    }

    public List<Producto> obtenerProductosDestacados() {
        return productoRepository.findByDestacadoTrueAndActivoTrue();
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    public List<Producto> buscarPorRangoPrecio(Double precioMin, Double precioMax) {
        return productoRepository.findByPrecioBetweenAndActivoTrue(precioMin, precioMax);
    }

    public Producto aplicarDescuento(Long id, Double porcentaje) {
        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (productoOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Producto producto = productoOpt.get();
        Double precioOriginal = producto.getPrecio();
        Double descuento = precioOriginal * (porcentaje / 100);
        Double precioFinal = precioOriginal - descuento;

        producto.setPrecioDescuento(precioFinal);
        return productoRepository.save(producto);
    }

    public Producto quitarDescuento(Long id) {
        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (productoOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Producto producto = productoOpt.get();
        producto.setPrecioDescuento(null);
        return productoRepository.save(producto);
    }

    public Producto actualizarStock(Long id, Integer cantidad) {
        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (productoOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Producto producto = productoOpt.get();
        producto.setStock(cantidad);
        return productoRepository.save(producto);
    }

    public Producto reducirStock(Long id, Integer cantidad) {
        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (productoOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Producto producto = productoOpt.get();

        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        producto.setStock(producto.getStock() - cantidad);
        return productoRepository.save(producto);
    }

    public Producto toggleDestacado(Long id) {
        Optional<Producto> productoOpt = productoRepository.findById(id);

        if (productoOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        Producto producto = productoOpt.get();
        producto.setDestacado(!producto.getDestacado());
        return productoRepository.save(producto);
    }

    public List<Producto> buscarConFiltros(String categoria, Double precioMin,
                                           Double precioMax, String color) {
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
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (query != null && !query.trim().isEmpty()) {
            return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(query, pageable);
        }
        return productoRepository.findByActivoTrue(pageable);
    }
}