package com.barefoot.service;

import com.barefoot.model.CarritoItem;
import com.barefoot.model.ItemCarrito;
import com.barefoot.model.Producto;
import com.barefoot.model.Usuario;
import com.barefoot.repository.CarritoItemRepository;
import com.barefoot.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarritoService {

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final String CARRITO_SESSION_KEY = "carrito";

    // --- 1. OBTENER CARRITO (HÍBRIDO) ---
    public List<ItemCarrito> obtenerCarrito(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId != null) {
            // MODO BD: Si está logueado, leemos de la base de datos
            Usuario usuario = new Usuario();
            usuario.setId(usuarioId);
            List<CarritoItem> itemsBD = carritoItemRepository.findByUsuario(usuario);

            // Convertimos la Entidad (BD) al DTO (Vista) para no romper el HTML
            return itemsBD.stream().map(item -> {
                ItemCarrito dto = new ItemCarrito(item.getProducto(), item.getCantidad());
                dto.setPersonalizacion(item.getPersonalizacion());
                return dto;
            }).collect(Collectors.toList());
        } else {
            // MODO SESIÓN: Si no está logueado, usamos la RAM
            return obtenerCarritoSesion(session);
        }
    }

    // --- 2. AGREGAR PRODUCTO (HÍBRIDO) ---
    @Transactional
    public void agregarProducto(HttpSession session, Producto producto, Integer cantidad, String personalizacion) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId != null) {
            // MODO BD
            Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();

            // ¿Ya existe este producto con esa personalización en su carrito guardado?
            Optional<CarritoItem> existente = carritoItemRepository
                    .findByUsuarioAndProductoIdAndPersonalizacion(usuario, producto.getId(), personalizacion);

            if (existente.isPresent()) {
                CarritoItem item = existente.get();
                item.setCantidad(item.getCantidad() + cantidad);
                carritoItemRepository.save(item);
            } else {
                CarritoItem nuevo = new CarritoItem(usuario, producto, cantidad, personalizacion);
                carritoItemRepository.save(nuevo);
            }
        } else {
            // MODO SESIÓN (Tu código original)
            List<ItemCarrito> carrito = obtenerCarritoSesion(session);
            Optional<ItemCarrito> existente = buscarEnSesion(carrito, producto.getId(), personalizacion);

            if (existente.isPresent()) {
                existente.get().incrementarCantidad(cantidad);
            } else {
                ItemCarrito nuevo = new ItemCarrito(producto, cantidad, personalizacion);
                carrito.add(nuevo);
            }
            session.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
        actualizarContador(session);
    }

    // --- 3. ACTUALIZAR CANTIDAD ---
    @Transactional
    public void actualizarCantidad(HttpSession session, Long productoId, Integer cantidad) {
        if (cantidad <= 0) {
            eliminarProducto(session, productoId);
            return;
        }

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (usuarioId != null) {
            // MODO BD
            Usuario usuario = new Usuario(); usuario.setId(usuarioId);
            // Nota: Esto actualiza el primer item que encuentre con ese productoId.
            // Idealmente se debería usar el ID del item del carrito, pero mantenemos productoId por compatibilidad.
            List<CarritoItem> items = carritoItemRepository.findByUsuario(usuario);
            for (CarritoItem item : items) {
                if (item.getProducto().getId().equals(productoId)) {
                    item.setCantidad(cantidad);
                    carritoItemRepository.save(item);
                    break;
                }
            }
        } else {
            // MODO SESIÓN
            List<ItemCarrito> carrito = obtenerCarritoSesion(session);
            carrito.stream().filter(i -> i.getProductoId().equals(productoId)).findFirst()
                    .ifPresent(i -> i.setCantidad(cantidad));
            session.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
        actualizarContador(session);
    }

    // --- 4. ELIMINAR PRODUCTO ---
    @Transactional
    public void eliminarProducto(HttpSession session, Long productoId) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId != null) {
            Usuario usuario = new Usuario(); usuario.setId(usuarioId);
            carritoItemRepository.deleteByUsuarioAndProductoId(usuario, productoId);
        } else {
            List<ItemCarrito> carrito = obtenerCarritoSesion(session);
            carrito.removeIf(i -> i.getProductoId().equals(productoId));
            session.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
        actualizarContador(session);
    }

    // --- 5. VACIAR CARRITO ---
    @Transactional
    public void vaciarCarrito(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId != null) {
            Usuario usuario = new Usuario(); usuario.setId(usuarioId);
            carritoItemRepository.deleteByUsuario(usuario);
        } else {
            session.removeAttribute(CARRITO_SESSION_KEY);
        }
        session.setAttribute("cantidadCarrito", 0);
    }

    // --- 6. FUSIONAR CARRITO (LO NUEVO) ---
    // Se llama al iniciar sesión para mover lo temporal a la BD
    @Transactional
    public void fusionarCarrito(HttpSession session, Usuario usuario) {
        List<ItemCarrito> carritoSesion = obtenerCarritoSesion(session);

        if (!carritoSesion.isEmpty()) {
            for (ItemCarrito itemSesion : carritoSesion) {
                Optional<CarritoItem> enBD = carritoItemRepository
                        .findByUsuarioAndProductoIdAndPersonalizacion(usuario, itemSesion.getProductoId(), itemSesion.getPersonalizacion());

                if (enBD.isPresent()) {
                    CarritoItem item = enBD.get();
                    item.setCantidad(item.getCantidad() + itemSesion.getCantidad());
                    carritoItemRepository.save(item);
                } else {
                    Producto p = new Producto(); p.setId(itemSesion.getProductoId());
                    CarritoItem nuevo = new CarritoItem(usuario, p, itemSesion.getCantidad(), itemSesion.getPersonalizacion());
                    carritoItemRepository.save(nuevo);
                }
            }
            // Limpiamos la sesión porque ya está seguro en la BD
            session.removeAttribute(CARRITO_SESSION_KEY);
        }
        actualizarContador(session);
    }

    // --- MÉTODOS AUXILIARES ---
    public Double calcularTotal(HttpSession session) {
        return obtenerCarrito(session).stream().mapToDouble(ItemCarrito::getSubtotal).sum();
    }

    public boolean estaVacio(HttpSession session) {
        return obtenerCarrito(session).isEmpty();
    }

    private void actualizarContador(HttpSession session) {
        int cantidad = obtenerCarrito(session).stream().mapToInt(ItemCarrito::getCantidad).sum();
        session.setAttribute("cantidadCarrito", cantidad);
    }

    @SuppressWarnings("unchecked")
    private List<ItemCarrito> obtenerCarritoSesion(HttpSession session) {
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute(CARRITO_SESSION_KEY);
        return carrito != null ? carrito : new ArrayList<>();
    }

    private Optional<ItemCarrito> buscarEnSesion(List<ItemCarrito> carrito, Long prodId, String pers) {
        return carrito.stream()
                .filter(i -> i.getProductoId().equals(prodId) &&
                        ((pers == null && i.getPersonalizacion() == null) || (pers != null && pers.equals(i.getPersonalizacion()))))
                .findFirst();
    }
}