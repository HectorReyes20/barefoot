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

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(nullable = false)
    private Double monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTransaccion estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Pasarela pasarela;

    @Column(length = 255)
    private String referenciaExterna;

    @Column(length = 500)
    private String tokenPago;

    @Column(length = 500)
    private String messageError;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column
    private LocalDateTime fechaConfirmacion;

    public enum EstadoTransaccion {
        PENDIENTE,
        PROCESANDO,
        COMPLETADO,
        FALLIDO,
        REEMBOLSADO
    }

    public enum Pasarela {
        STRIPE,
        IZIPAY,
        PAGOSEGURO
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoTransaccion.PENDIENTE;
        }
    }
}

