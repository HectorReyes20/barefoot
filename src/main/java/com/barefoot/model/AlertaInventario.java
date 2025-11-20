package com.barefoot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertas_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlerta tipo;

    @Column(nullable = false)
    private String mensaje;

    @Column(name = "stock_actual")
    private Integer stockActual;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(nullable = false)
    private Boolean leida = false;

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_leida")
    private LocalDateTime fechaLeida;

    @ManyToOne
    @JoinColumn(name = "leida_por")
    private Usuario leidaPor;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    public enum TipoAlerta {
        STOCK_BAJO("Stock Bajo", "warning", "fa-exclamation-triangle"),
        STOCK_CRITICO("Stock Crítico", "danger", "fa-exclamation-circle"),
        STOCK_AGOTADO("Stock Agotado", "danger", "fa-times-circle"),
        STOCK_EXCESO("Stock en Exceso", "info", "fa-info-circle"),
        MOVIMIENTO_SOSPECHOSO("Movimiento Sospechoso", "warning", "fa-question-circle");

        private final String nombre;
        private final String colorBadge;
        private final String icono;

        TipoAlerta(String nombre, String colorBadge, String icono) {
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

    // Constructor para crear alertas fácilmente
    public AlertaInventario(Producto producto, TipoAlerta tipo, String mensaje, Integer stockActual) {
        this.producto = producto;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.stockActual = stockActual;
        this.stockMinimo = 10; // Valor por defecto
    }

    public void marcarComoLeida(Usuario usuario) {
        this.leida = true;
        this.fechaLeida = LocalDateTime.now();
        this.leidaPor = usuario;
    }

    public void desactivar() {
        this.activa = false;
    }
}