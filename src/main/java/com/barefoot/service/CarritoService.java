package com.barefoot.service;

import com.barefoot.model.ItemCarrito;
import com.barefoot.model.Producto;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {

    private static final String CARRITO_SESSION_KEY = "carrito";

    // Obtener el carrito de la sesión
    @SuppressWarnings("unchecked")
    public List<ItemCarrito> obtenerCarrito(HttpSession session) {
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute(CARRITO_SESSION_KEY);
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
        return carrito;
    }

    // Agregar producto al carrito
    public void agregarProducto(HttpSession session, Producto producto, Integer cantidad, String personalizacion) {
        List<ItemCarrito> carrito = obtenerCarrito(session);

        // Buscar si el producto ya existe en el carrito
        Optional<ItemCarrito> itemExistente = carrito.stream()
                .filter(item -> item.getProductoId().equals(producto.getId()) &&
                        ((personalizacion == null && item.getPersonalizacion() == null) ||
                                (personalizacion != null && personalizacion.equals(item.getPersonalizacion()))))
                .findFirst();

        if (itemExistente.isPresent()) {
            // Si existe, incrementar cantidad
            itemExistente.get().incrementarCantidad(cantidad);
        } else {
            // Si no existe, agregar nuevo item
            ItemCarrito nuevoItem;
            if (personalizacion != null && !personalizacion.isEmpty()) {
                nuevoItem = new ItemCarrito(producto, cantidad, personalizacion);
            } else {
                nuevoItem = new ItemCarrito(producto, cantidad);
            }
            carrito.add(nuevoItem);
        }

        session.setAttribute(CARRITO_SESSION_KEY, carrito);
        actualizarContadorCarrito(session);
    }

    // Actualizar cantidad de un item
    public void actualizarCantidad(HttpSession session, Long productoId, Integer cantidad) {
        List<ItemCarrito> carrito = obtenerCarrito(session);

        if (cantidad <= 0) {
            eliminarProducto(session, productoId);
            return;
        }

        carrito.stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst()
                .ifPresent(item -> item.setCantidad(cantidad));

        session.setAttribute(CARRITO_SESSION_KEY, carrito);
        actualizarContadorCarrito(session);
    }

    // Eliminar producto del carrito
    public void eliminarProducto(HttpSession session, Long productoId) {
        List<ItemCarrito> carrito = obtenerCarrito(session);
        carrito.removeIf(item -> item.getProductoId().equals(productoId));
        session.setAttribute(CARRITO_SESSION_KEY, carrito);
        actualizarContadorCarrito(session);
    }

    // Vaciar el carrito
    public void vaciarCarrito(HttpSession session) {
        session.setAttribute(CARRITO_SESSION_KEY, new ArrayList<ItemCarrito>());
        session.setAttribute("cantidadCarrito", 0);
    }

    // Calcular el total del carrito
    public Double calcularTotal(HttpSession session) {
        List<ItemCarrito> carrito = obtenerCarrito(session);
        return carrito.stream()
                .mapToDouble(ItemCarrito::getSubtotal)
                .sum();
    }

    // Calcular la cantidad total de items
    public Integer calcularCantidadTotal(HttpSession session) {
        List<ItemCarrito> carrito = obtenerCarrito(session);
        return carrito.stream()
                .mapToInt(ItemCarrito::getCantidad)
                .sum();
    }

    // Actualizar el contador del carrito en la sesión
    private void actualizarContadorCarrito(HttpSession session) {
        Integer cantidad = calcularCantidadTotal(session);
        session.setAttribute("cantidadCarrito", cantidad);
    }

    // Verificar si el carrito está vacío
    public boolean estaVacio(HttpSession session) {
        return obtenerCarrito(session).isEmpty();
    }
}