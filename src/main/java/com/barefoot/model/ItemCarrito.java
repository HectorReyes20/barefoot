package com.barefoot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarrito {

    private Long productoId;
    private String nombre;
    private String imagenUrl;
    private Double precio;
    private Integer cantidad;
    private String personalizacion;

    // Constructor principal
    public ItemCarrito(Producto producto, Integer cantidad) {
        this.productoId = producto.getId();
        this.nombre = producto.getNombre();
        this.imagenUrl = producto.getImagenUrl();
        this.precio = producto.getPrecioFinal();
        this.cantidad = cantidad;
    }

    // Constructor con personalización
    public ItemCarrito(Producto producto, Integer cantidad, String personalizacion) {
        this(producto, cantidad);
        this.personalizacion = personalizacion;
    }

    // Calcular subtotal del item
    public Double getSubtotal() {
        return precio * cantidad;
    }

    // Incrementar cantidad
    public void incrementarCantidad(Integer cantidad) {
        this.cantidad += cantidad;
    }
}