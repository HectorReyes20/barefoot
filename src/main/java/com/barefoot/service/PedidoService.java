package com.barefoot.service;

import com.barefoot.model.DetallePedido;
import com.barefoot.model.Pedido;
import com.barefoot.model.Producto;
import com.barefoot.model.Usuario;
import com.barefoot.repository.PedidoRepository;
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
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoService productoService;

    // Crear pedido
    public Pedido crearPedido(Pedido pedido) {

        // Validar stock de productos
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto.getStock() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }
        }

        // Calcular totales
        pedido.calcularTotal();

        // Guardar pedido
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Reducir stock de productos
        for (DetallePedido detalle : pedidoGuardado.getDetalles()) {
            productoService.reducirStock(detalle.getProducto().getId(), detalle.getCantidad());
        }

        return pedidoGuardado;
    }

    // Obtener todos los pedidos
    public List<Pedido> obtenerTodosPedidos() {
        return pedidoRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaPedido"));
    }

    // Obtener pedidos con paginación
    public Page<Pedido> obtenerPedidosConPaginacion(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPedido").descending());
        return pedidoRepository.findAllByOrderByFechaPedidoDesc(pageable);
    }

    // Obtener pedido por ID (VERSIÓN CORRECTA SIN MERGE CONFLICT)
    public Optional<Pedido> obtenerPedidoPorId(Long id) {
        return pedidoRepository.findByIdWithDetallesAndProductos(id);
    }

    // Obtener pedido por número
    public Optional<Pedido> obtenerPedidoPorNumero(String numeroPedido) {
        return pedidoRepository.findByNumeroPedido(numeroPedido);
    }

    // Obtener pedidos de un usuario
    public List<Pedido> obtenerPedidosDeUsuario(Usuario usuario) {
        return pedidoRepository.findByUsuarioOrderByFechaPedidoDesc(usuario);
    }

    // Obtener pedidos por estado
    public List<Pedido> obtenerPedidosPorEstado(Pedido.EstadoPedido estado) {
        return pedidoRepository.findByEstadoOrderByFechaPedidoDesc(estado);
    }

    // Actualizar estado del pedido
    public Pedido actualizarEstado(Long id, Pedido.EstadoPedido nuevoEstado) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);

        if (pedidoOpt.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado con ID: " + id);
        }

        Pedido pedido = pedidoOpt.get();
        pedido.setEstado(nuevoEstado);

        // Si se marca como entregado, registrar fecha
        if (nuevoEstado == Pedido.EstadoPedido.ENTREGADO) {
            pedido.setFechaEntrega(LocalDateTime.now());
        }

        // Si se cancela, devolver stock
        if (nuevoEstado == Pedido.EstadoPedido.CANCELADO) {
            devolverStockPedido(pedido);
        }

        return pedidoRepository.save(pedido);
    }

    // Cancelar pedido
    public Pedido cancelarPedido(Long id, String motivo) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);

        if (pedidoOpt.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado");
        }

        Pedido pedido = pedidoOpt.get();

        // Solo se pueden cancelar pedidos confirmados
        if (pedido.getEstado() != Pedido.EstadoPedido.CONFIRMADO) {
            throw new RuntimeException("No se puede cancelar un pedido en estado: " + pedido.getEstado());
        }

        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);
        if (motivo != null) {
            pedido.setNotas(pedido.getNotas() + "\nMotivo cancelación: " + motivo);
        }

        // Devolver stock
        devolverStockPedido(pedido);

        return pedidoRepository.save(pedido);
    }

    // Devolver stock de productos
    private void devolverStockPedido(Pedido pedido) {
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoService.actualizarStock(producto.getId(), producto.getStock());
        }
    }

    // Estadísticas
    public Long contarPedidosPorEstado(Pedido.EstadoPedido estado) {
        return pedidoRepository.countByEstado(estado);
    }

    public Double calcularTotalVentas() {
        Double total = pedidoRepository.calcularTotalVentas();
        return total != null ? total : 0.0;
    }

    public Double calcularVentasDelMes() {
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime finMes = LocalDateTime.now();
        Double total = pedidoRepository.calcularVentasPorFecha(inicioMes, finMes);
        return total != null ? total : 0.0;
    }

    public List<Pedido> obtenerPedidosRecientes(int limite) {
        return pedidoRepository.findTop10ByOrderByFechaPedidoDesc()
                .stream()
                .limit(limite)
                .toList();
    }

    // Actualizar información editable del pedido
    public Pedido actualizarPedido(Long id, Pedido pedidoActualizado) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);

        if (pedidoOpt.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado");
        }

        Pedido pedido = pedidoOpt.get();

        if (pedidoActualizado.getDireccionEnvio() != null) {
            pedido.setDireccionEnvio(pedidoActualizado.getDireccionEnvio());
        }
        if (pedidoActualizado.getNotas() != null) {
            pedido.setNotas(pedidoActualizado.getNotas());
        }
        if (pedidoActualizado.getMetodoPago() != null) {
            pedido.setMetodoPago(pedidoActualizado.getMetodoPago());
        }

        return pedidoRepository.save(pedido);
    }

    // Buscar pedidos por rango de fechas
    public List<Pedido> buscarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return pedidoRepository.findByFechaPedidoBetween(inicio, fin);
    }
}
