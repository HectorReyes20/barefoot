package com.barefoot.repository;

import com.barefoot.model.CarritoItem;
import com.barefoot.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {

    // Buscar todos los items de un usuario
    List<CarritoItem> findByUsuario(Usuario usuario);

    // Buscar un item específico (para sumar cantidad si ya existe)
    Optional<CarritoItem> findByUsuarioAndProductoIdAndPersonalizacion(Usuario usuario, Long productoId, String personalizacion);

    // Borrar todo el carrito de un usuario (al comprar)
    void deleteByUsuario(Usuario usuario);

    // Borrar un producto específico
    void deleteByUsuarioAndProductoId(Usuario usuario, Long productoId);
}