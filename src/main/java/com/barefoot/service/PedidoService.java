package com.barefoot.service;

import com.barefoot.model.*;
import com.barefoot.repository.DetallePedidoRepository;
import com.barefoot.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private ProductoService productoService;

    // =================================================================
    // NUEVO MÉTODO: CREAR PEDIDO DESDE CARRITO (CORREGIDO)
    // =================================================================
    public Pedido crearPedidoDesdeCarrito(Usuario usuario, Carrito carrito, String metodoPagoStr) {

        // 1. Validar Stock (Buscando el producto real por ID)
        for (ItemCarrito item : carrito.getItems()) {
            Producto productoReal = productoService.obtenerProductoPorId(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getNombre()));

            if (productoReal.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + productoReal.getNombre());
            }
        }

        // 2. Crear Cabecera del Pedido
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstado(Pedido.EstadoPedido.CONFIRMADO); // Nace confirmado

        // Asignar método
        if ("YAPE".equalsIgnoreCase(metodoPagoStr)) {
            pedido.setMetodoPago(Pedido.MetodoPago.YAPE);
        } else {
            pedido.setMetodoPago(Pedido.MetodoPago.STRIPE);
        }

        // Calcular totales
        pedido.setSubtotal(carrito.getTotal());
        pedido.setCostoEnvio(15.0);
        pedido.setTotal(pedido.getSubtotal() + pedido.getCostoEnvio());

        // Guardar para generar ID
        pedido = pedidoRepository.save(pedido);

        // 3. Crear Detalles y Reducir Stock
        List<DetallePedido> detalles = new ArrayList<>();

        for (ItemCarrito item : carrito.getItems()) {
            // BUSCAMOS EL PRODUCTO REAL POR ID (Corrección del error getProducto)
            Producto productoReal = productoService.obtenerProductoPorId(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + item.getProductoId()));

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(productoReal); // Usamos el objeto encontrado
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(productoReal.getPrecio()); // O productoReal.getPrecioFinal() si tienes descuentos
            detalle.setSubtotal(item.getSubtotal());

            // --- AQUÍ ESTÁ LA LÍNEA QUE TE FALTABA PARA LA PERSONALIZACIÓN ---
            detalle.setPersonalizacion(item.getPersonalizacion());
            // ------------------------------------------------------------------

            // REDUCIR STOCK REAL
            productoService.reducirStock(productoReal.getId(), item.getCantidad());

            detalles.add(detalle);
        }

        // Guardar todos los detalles en BD
        detallePedidoRepository.saveAll(detalles);
        pedido.setDetalles(detalles);

        return pedido;
    }

    // =================================================================
    // MÉTODOS EXISTENTES (AJUSTADOS)
    // =================================================================

    // Crear pedido manual (Legacy)
    public Pedido crearPedido(Pedido pedido) {
        // Validar stock
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto.getStock() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }
        }

        // Calcular totales manualmente (Corrección del error calcularTotal)
        double subtotal = pedido.getDetalles().stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();
        pedido.setSubtotal(subtotal);
        pedido.setTotal(subtotal - pedido.getDescuento() + pedido.getCostoEnvio());

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Reducir stock
        for (DetallePedido detalle : pedidoGuardado.getDetalles()) {
            productoService.reducirStock(detalle.getProducto().getId(), detalle.getCantidad());
        }
        return pedidoGuardado;
    }

    public List<Pedido> obtenerTodosPedidos() {
        return pedidoRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaPedido"));
    }

    public Page<Pedido> obtenerPedidosConPaginacion(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPedido").descending());
        return pedidoRepository.findAllByOrderByFechaPedidoDesc(pageable);
    }

    public Optional<Pedido> obtenerPedidoPorId(Long id) {
        try {
            return pedidoRepository.findByIdWithDetallesAndProductos(id);
        } catch (Exception e) {
            return pedidoRepository.findById(id);
        }
    }

    public Optional<Pedido> obtenerPedidoPorNumero(String numeroPedido) {
        return pedidoRepository.findByNumeroPedido(numeroPedido);
    }

    public List<Pedido> obtenerPedidosDeUsuario(Usuario usuario) {
        return pedidoRepository.findByUsuarioOrderByFechaPedidoDesc(usuario);
    }

    public List<Pedido> obtenerPedidosPorEstado(Pedido.EstadoPedido estado) {
        return pedidoRepository.findByEstadoOrderByFechaPedidoDesc(estado);
    }

    public Pedido actualizarEstado(Long id, Pedido.EstadoPedido nuevoEstado) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        if (pedidoOpt.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado con ID: " + id);
        }

        Pedido pedido = pedidoOpt.get();
        pedido.setEstado(nuevoEstado);

        if (nuevoEstado == Pedido.EstadoPedido.ENTREGADO) {
            pedido.setFechaEntrega(LocalDateTime.now());
        }

        if (nuevoEstado == Pedido.EstadoPedido.CANCELADO) {
            devolverStockPedido(pedido);
        }

        return pedidoRepository.save(pedido);
    }

    // Cancelar pedido (CORREGIDO: Sin ENVIADO)
    public Pedido cancelarPedido(Long id, String motivo) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        if (pedidoOpt.isEmpty()) {
            throw new RuntimeException("Pedido no encontrado");
        }

        Pedido pedido = pedidoOpt.get();

        // Corrección: Eliminado ENVIADO
        if (pedido.getEstado() == Pedido.EstadoPedido.EN_CAMINO ||
                pedido.getEstado() == Pedido.EstadoPedido.ENTREGADO) {

            throw new RuntimeException("No se puede cancelar un pedido que ya está en camino o entregado.");
        }

        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);
        if (motivo != null) {
            pedido.setNotas(pedido.getNotas() + "\nMotivo cancelación: " + motivo);
        }

        devolverStockPedido(pedido);
        return pedidoRepository.save(pedido);
    }

    private void devolverStockPedido(Pedido pedido) {
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoService.actualizarStock(producto.getId(), producto.getStock());
        }
    }

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

    public Pedido actualizarPedido(Long id, Pedido pedidoActualizado) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        if (pedidoOpt.isEmpty()) throw new RuntimeException("Pedido no encontrado");

        Pedido pedido = pedidoOpt.get();
        if (pedidoActualizado.getDireccionEnvio() != null) pedido.setDireccionEnvio(pedidoActualizado.getDireccionEnvio());
        if (pedidoActualizado.getNotas() != null) pedido.setNotas(pedidoActualizado.getNotas());
        if (pedidoActualizado.getMetodoPago() != null) pedido.setMetodoPago(pedidoActualizado.getMetodoPago());

        return pedidoRepository.save(pedido);
    }

    public List<Pedido> buscarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return pedidoRepository.findByFechaPedidoBetween(inicio, fin);
    }
}