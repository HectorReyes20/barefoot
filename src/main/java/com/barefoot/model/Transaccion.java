package com.barefoot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaccion")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CAMBIO CRÍTICO: nullable = true (antes era false)
    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = true)
    private Pedido pedido;

    @Column(nullable = false)
    private Double monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTransaccion estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Pasarela pasarela;

    /**
     * Para Stripe: Payment Intent ID
     * Para Yape: Código de aprobación de 6 dígitos
     */
    @Column(length = 255)
    private String referenciaExterna;

    /**
     * Solo para Stripe: Client Secret para el frontend
     */
    @Column(length = 500)
    private String tokenPago;

    @Column(length = 500)
    private String messageError;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column
    private LocalDateTime fechaConfirmacion;

    public enum EstadoTransaccion {
        PENDIENTE,      // Yape esperando confirmación del admin, Stripe procesando
        PROCESANDO,     // Stripe en proceso
        COMPLETADO,     // Pago confirmado (Stripe automático, Yape por admin)
        FALLIDO,        // Pago rechazado o falló
        REEMBOLSADO     // Pago devuelto
    }

    public enum Pasarela {
        STRIPE("Tarjeta de Crédito/Débito", "💳"),
        YAPE("Yape", "📱");

        private final String nombre;
        private final String icono;

        Pasarela(String nombre, String icono) {
            this.nombre = nombre;
            this.icono = icono;
        }

        public String getNombre() {
            return nombre;
        }

        public String getIcono() {
            return icono;
        }
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoTransaccion.PENDIENTE;
        }
    }

    /**
     * Helper para saber si es pago con tarjeta
     */
    public boolean esPagoTarjeta() {
        return this.pasarela == Pasarela.STRIPE;
    }

    /**
     * Helper para saber si es pago Yape
     */
    public boolean esPagoYape() {
        return this.pasarela == Pasarela.YAPE;
    }

    /**
     * Obtener descripción legible del método de pago
     */
    public String getMetodoPagoDescripcion() {
        return pasarela.getIcono() + " " + pasarela.getNombre();
    }
}