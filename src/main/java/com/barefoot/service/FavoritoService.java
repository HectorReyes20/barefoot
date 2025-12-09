package com.barefoot.service;

import com.barefoot.model.Favorito;
import com.barefoot.model.Producto;
import com.barefoot.model.Usuario;
import com.barefoot.repository.FavoritoRepository;
import com.barefoot.repository.ProductoRepository;
import com.barefoot.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FavoritoService {

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<Favorito> listarFavoritos(Long usuarioId) {
        Usuario usuario = new Usuario(); usuario.setId(usuarioId);
        return favoritoRepository.findByUsuario(usuario);
    }

    @Transactional
    public boolean toggleFavorito(Long usuarioId, Long productoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        Producto producto = productoRepository.findById(productoId).orElseThrow();

        Optional<Favorito> existente = favoritoRepository.findByUsuarioAndProducto(usuario, producto);

        if (existente.isPresent()) {
            favoritoRepository.delete(existente.get());
            return false; // Eliminado (ya no es favorito)
        } else {
            favoritoRepository.save(new Favorito(usuario, producto));
            return true; // Agregado (ahora es favorito)
        }
    }

    public boolean esFavorito(Long usuarioId, Long productoId) {
        Usuario u = new Usuario(); u.setId(usuarioId);
        Producto p = new Producto(); p.setId(productoId);
        return favoritoRepository.existsByUsuarioAndProducto(u, p);
    }
}