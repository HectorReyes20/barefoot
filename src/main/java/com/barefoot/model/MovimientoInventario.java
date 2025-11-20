package com.barefoot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_nuevo", nullable = false)
    private Integer stockNuevo;

    @Column(length = 500)
    private String motivo;

    @Column(length = 500)
    private String referencia;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;

    @Column(length = 100)
    private String numeroDocumento;

    @PrePersist
    protected void onCreate() {
        fechaMovimiento = LocalDateTime.now();
    }

    public enum TipoMovimiento {
        ENTRADA("Entrada", "success", "fa-arrow-up"),
        SALIDA("Salida", "danger", "fa-arrow-down"),
        AJUSTE_ENTRADA("Ajuste Entrada", "info", "fa-plus-circle"),
        AJUSTE_SALIDA("Ajuste Salida", "warning", "fa-minus-circle"),
        DEVOLUCION("Devolución", "primary", "fa-undo"),
        MERMA("Merma", "dark", "fa-trash"),
        TRANSFERENCIA("Transferencia", "secondary", "fa-exchange-alt");

        private final String nombre;
        private final String colorBadge;
        private final String icono;

        TipoMovimiento(String nombre, String colorBadge, String icono) {
            this.nombre = nombre;
            this.colorBadge = colorBadge;
            this.icono = icono;
        }

        public String getNombre() {
            return nombre;
        }

        public String getColorBadge() {
            return colorBadge;
        }

        public String getIcono() {
            return icono;
        }
    }

    // Constructor para crear movimientos fácilmente
    public MovimientoInventario(Producto producto, TipoMovimiento tipo, Integer cantidad,
                                String motivo, Usuario usuario) {
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.stockAnterior = producto.getStock();

        // Calcular nuevo stock según el tipo
        if (esEntrada(tipo)) {
            this.stockNuevo = stockAnterior + cantidad;
        } else {
            this.stockNuevo = stockAnterior - cantidad;
        }

        this.motivo = motivo;
        this.usuario = usuario;
    }

    private boolean esEntrada(TipoMovimiento tipo) {
        return tipo == TipoMovimiento.ENTRADA ||
                tipo == TipoMovimiento.AJUSTE_ENTRADA ||
                tipo == TipoMovimiento.DEVOLUCION;
    }

    public boolean aumentaStock() {
        return esEntrada(this.tipo);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void setStockAnterior(Integer stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public void setStockNuevo(Integer stockNuevo) {
        this.stockNuevo = stockNuevo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public Long getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public Integer getStockAnterior() {
        return stockAnterior;
    }

    public Integer getStockNuevo() {
        return stockNuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getReferencia() {
        return referencia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }
}