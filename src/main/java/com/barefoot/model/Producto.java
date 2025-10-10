package com.barefoot.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Column(name = "precio_descuento")
    private Double precioDescuento;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(length = 50)
    private String categoria;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 50)
    private String talla;

    @Column(length = 50)
    private String color;

    @Column(length = 100)
    private String material;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Boolean destacado = false;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // Metodo para verificar si tiene descuento
    public boolean tieneDescuento() {
        return precioDescuento != null && precioDescuento < precio;
    }

    // Metodo para obtener el precio final
    public Double getPrecioFinal() {
        return tieneDescuento() ? precioDescuento : precio;
    }

    // Metodo para calcular porcentaje de descuento
    public Integer getPorcentajeDescuento() {
        if (tieneDescuento()) {
            return (int) (((precio - precioDescuento) / precio) * 100);
        }
        return 0;
    }
}
