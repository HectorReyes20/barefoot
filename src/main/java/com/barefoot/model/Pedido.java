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

    @Column(name = "numero_pedido", unique = true, nullable = false)
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

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
            numeroPedido = generarNumeroPedido();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    private String generarNumeroPedido() {
        return "PED-" + System.currentTimeMillis();
    }

    public void calcularTotal() {
        this.subtotal = detalles.stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();
        this.total = subtotal - descuento + costoEnvio;
    }

    public int getCantidadTotalProductos() {
        return detalles.stream()
                .mapToInt(DetallePedido::getCantidad)
                .sum();
    }

    public enum EstadoPedido {
        PENDIENTE("Pendiente", "warning"),
        CONFIRMADO("Confirmado", "info"),
        PREPARANDO("Preparando", "primary"),
        ENVIADO("Enviado", "info"),
        EN_CAMINO("En Camino", "info"),
        ENTREGADO("Entregado", "success"),
        CANCELADO("Cancelado", "danger");

        private final String nombre;
        private final String colorBadge;

        EstadoPedido(String nombre, String colorBadge) {
            this.nombre = nombre;
            this.colorBadge = colorBadge;
        }

        public String getNombre() {
            return nombre;
        }

        public String getColorBadge() {
            return colorBadge;
        }
    }

    public enum MetodoPago {
        TARJETA_CREDITO("Tarjeta de Crédito"),
        TARJETA_DEBITO("Tarjeta de Débito"),
        TRANSFERENCIA("Transferencia Bancaria"),
        YAPE("Yape"),
        PLIN("Plin"),
        CONTRAENTREGA("Contra Entrega");

        private final String nombre;

        MetodoPago(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }
    }
}