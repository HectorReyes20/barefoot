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

    public boolean tieneDescuento() {
        return precioDescuento != null && precioDescuento < precio;
    }

    public Double getPrecioFinal() {
        return tieneDescuento() ? precioDescuento : precio;
    }

    public Integer getPorcentajeDescuento() {
        if (tieneDescuento()) {
            return (int) (((precio - precioDescuento) / precio) * 100);
        }
        return 0;
    }

    public Boolean getActivo() {
        return activo;
    }

    public Long getId() {
        return id;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public Double getPrecioDescuento() {
        return precioDescuento;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public Integer getStock() {
        return stock;
    }

    public String getTalla() {
        return talla;
    }

    public String getColor() {
        return color;
    }

    public String getMaterial() {
        return material;
    }

    public Boolean getDestacado() {
        return destacado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public void setPrecioDescuento(Double precioDescuento) {
        this.precioDescuento = precioDescuento;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public void setDestacado(Boolean destacado) {
        this.destacado = destacado;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
