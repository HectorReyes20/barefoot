package com.barefoot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "numero_pedido", length = 50, unique = true, nullable = false)
    private String numeroPedido;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double descuento = 0.0;

    @Column(name = "costo_envio", nullable = false)
    private Double costoEnvio = 0.0;

    @Column(nullable = false)
    private Double total;

    // ESTADO INICIAL: CONFIRMADO (Ya que solo se crea si paga)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.CONFIRMADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago;

    @Column(name = "direccion_envio", length = 500)
    private String direccionEnvio;

    @Column(length = 500)
    private String notas;

    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @PrePersist
    protected void onCreate() {
        fechaPedido = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (numeroPedido == null) {
            numeroPedido = "PED-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum EstadoPedido {
        CONFIRMADO("Confirmado", "success"),
        PREPARANDO("Preparando", "primary"),
        EN_CAMINO("En Camino", "info"),
        ENTREGADO("Entregado", "secondary"),
        CANCELADO("Cancelado", "danger");

        private final String nombre;
        private final String colorBadge;

        EstadoPedido(String nombre, String colorBadge) {
            this.nombre = nombre;
            this.colorBadge = colorBadge;
        }
        public String getNombre() { return nombre; }
        public String getColorBadge() { return colorBadge; }
    }

    public enum MetodoPago {
        STRIPE("Tarjeta de Crédito/Débito", "💳"),
        YAPE("Yape", "📱");

        private final String nombre;
        private final String icono;

        MetodoPago(String nombre, String icono) {
            this.nombre = nombre;
            this.icono = icono;
        }
        public String getNombre() { return nombre; }
        public String getIcono() { return icono; }
    }

    // --- MÉTODOS AUXILIARES PARA LA VISTA ---

    public int getCantidadTotalProductos() {
        if (detalles == null || detalles.isEmpty()) {
            return 0;
        }
        return detalles.stream()
                .mapToInt(DetallePedido::getCantidad)
                .sum();
    }

    // También agrega este por si acaso lo usas en otro lado
    public void calcularTotal() {
        if (detalles != null) {
            this.subtotal = detalles.stream()
                    .mapToDouble(DetallePedido::getSubtotal)
                    .sum();
            this.total = subtotal - descuento + costoEnvio;
        }
    }
}