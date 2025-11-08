package com.barefoot.service;

import com.barefoot.model.Producto;
import com.barefoot.repository.ProductoRepository;
import com.barefoot.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> estadisticas = new HashMap<>();

        // Estadísticas de productos
        List<Producto> todosProductos = productoRepository.findAll();
        long productosActivos = todosProductos.stream()
                .filter(Producto::getActivo)
                .count();
        long productosDestacados = todosProductos.stream()
                .filter(Producto::getDestacado)
                .count();
        long productosStockBajo = todosProductos.stream()
                .filter(p -> p.getStock() < 10)
                .count();

        estadisticas.put("totalProductos", todosProductos.size());
        estadisticas.put("productosActivos", productosActivos);
        estadisticas.put("productosDestacados", productosDestacados);
        estadisticas.put("productosStockBajo", productosStockBajo);

        // Estadísticas de usuarios
        long totalUsuarios = usuarioRepository.count();
        estadisticas.put("totalUsuarios", totalUsuarios);

        // Valor total del inventario
        double valorInventario = todosProductos.stream()
                .filter(Producto::getActivo)
                .mapToDouble(p -> p.getPrecio() * p.getStock())
                .sum();
        estadisticas.put("valorInventario", valorInventario);

        // Productos más vendidos (simulado por ahora)
        estadisticas.put("ventasMes", 45890.0);
        estadisticas.put("pedidosTotales", 245L);

        return estadisticas;
    }

    public List<Producto> obtenerProductosRecientes(int limite) {
        return productoRepository.findTop10ByActivoTrueOrderByFechaCreacionDesc()
                .stream()
                .limit(limite)
                .collect(Collectors.toList());
    }

    public List<Producto> obtenerProductosStockBajo() {
        return productoRepository.findAll().stream()
                .filter(p -> p.getStock() < 10)
                .sorted((p1, p2) -> Integer.compare(p1.getStock(), p2.getStock()))
                .collect(Collectors.toList());
    }

    public Map<String, Long> obtenerProductosPorCategoria() {
        List<Producto> productos = productoRepository.findByActivoTrue();
        return productos.stream()
                .collect(Collectors.groupingBy(
                        Producto::getCategoria,
                        Collectors.counting()
                ));
    }

    public List<Map<String, Object>> obtenerTopProductos(int limite) {
        return productoRepository.findByDestacadoTrueAndActivoTrue()
                .stream()
                .limit(limite)
                .map(p -> {
                    Map<String, Object> mapa = new HashMap<>();
                    mapa.put("id", p.getId());
                    mapa.put("nombre", p.getNombre());
                    mapa.put("precio", p.getPrecio());
                    mapa.put("stock", p.getStock());
                    mapa.put("imagenUrl", p.getImagenUrl());
                    mapa.put("ventas", Math.random() * 100); // Simulado
                    return mapa;
                })
                .collect(Collectors.toList());
    }
}