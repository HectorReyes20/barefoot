package com.barefoot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carrito_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(length = 1000)
    private String personalizacion;

    // Constructor auxiliar para facilitar la creación
    public CarritoItem(Usuario usuario, Producto producto, Integer cantidad, String personalizacion) {
        this.usuario = usuario;
        this.producto = producto;
        this.cantidad = cantidad;
        this.personalizacion = personalizacion;
    }
}