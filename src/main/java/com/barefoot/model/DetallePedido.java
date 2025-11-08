package com.barefoot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalle_pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    @Column(nullable = false)
    private Double subtotal;

    @Column(name = "personalizacion", length = 1000)
    private String personalizacion;

    @PrePersist
    @PreUpdate
    protected void calcularSubtotal() {
        this.subtotal = precioUnitario * cantidad;
    }

    public DetallePedido(Producto producto, Integer cantidad, String personalizacion) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecioFinal();
        this.personalizacion = personalizacion;
        calcularSubtotal();
    }
}