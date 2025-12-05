package com.barefoot.service;

import com.barefoot.model.Pedido;
import com.barefoot.model.Transaccion;
import com.barefoot.repository.TransaccionRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class PagoService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public PagoService() {
        // Stripe se inicializa con la key del application.properties
    }

    /**
     * Crear un PaymentIntent con Stripe para iniciar el pago
     */
    public Transaccion iniciarPagoStripe(Pedido pedido) throws StripeException {
        // Inicializar Stripe con la API key
        Stripe.apiKey = stripeApiKey;

        // Crear transacción en BD
        Transaccion transaccion = new Transaccion();
        transaccion.setPedido(pedido);
        transaccion.setMonto(pedido.getTotal());
        transaccion.setPasarela(Transaccion.Pasarela.STRIPE);
        transaccion.setEstado(Transaccion.EstadoTransaccion.PENDIENTE);

        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        // Crear PaymentIntent en Stripe
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (pedido.getTotal() * 100)) // Stripe usa centavos
                .setCurrency("pen") // Soles peruanos
                .setDescription("Pedido #" + pedido.getNumeroPedido())
                .putMetadata("pedido_id", pedido.getId().toString())
                .putMetadata("transaccion_id", transaccionGuardada.getId().toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        // Guardar referencia de Stripe
        transaccionGuardada.setReferenciaExterna(intent.getId());
        transaccionGuardada.setTokenPago(intent.getClientSecret());
        transaccionGuardada.setEstado(Transaccion.EstadoTransaccion.PROCESANDO);

        return transaccionRepository.save(transaccionGuardada);
    }

    /**
     * Confirmar pago después de que Stripe lo procese
     */
    public Transaccion confirmarPago(String paymentIntentId) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        // Obtener el PaymentIntent de Stripe
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

        // Buscar transacción
        Optional<Transaccion> transaccionOpt = transaccionRepository.findByReferenciaExterna(paymentIntentId);

        if (transaccionOpt.isEmpty()) {
            throw new RuntimeException("Transacción no encontrada: " + paymentIntentId);
        }

        Transaccion transaccion = transaccionOpt.get();

        // Verificar estado del pago
        if ("succeeded".equals(intent.getStatus())) {
            transaccion.setEstado(Transaccion.EstadoTransaccion.COMPLETADO);
            transaccion.setFechaConfirmacion(LocalDateTime.now());

            // Actualizar estado del pedido
            Pedido pedido = transaccion.getPedido();
            pedido.setEstado(Pedido.EstadoPedido.CONFIRMADO);

            log.info("Pago confirmado para pedido: {}", pedido.getNumeroPedido());
        } else if ("processing".equals(intent.getStatus())) {
            transaccion.setEstado(Transaccion.EstadoTransaccion.PROCESANDO);
            log.info("Pago en procesamiento para pedido: {}", transaccion.getPedido().getNumeroPedido());
        } else {
            transaccion.setEstado(Transaccion.EstadoTransaccion.FALLIDO);
            transaccion.setMessageError(intent.getLastPaymentError() != null ?
                intent.getLastPaymentError().getMessage() : "Error desconocido");
            log.error("Pago fallido para pedido: {}", transaccion.getPedido().getNumeroPedido());
        }

        return transaccionRepository.save(transaccion);
    }

    /**
     * Obtener estado de una transacción
     */
    public Transaccion obtenerEstado(Long transaccionId) {
        return transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
    }

    /**
     * Obtener transacción por referencia de Stripe
     */
    public Transaccion obtenerPorReferenciaExterna(String referencia) {
        return transaccionRepository.findByReferenciaExterna(referencia)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada: " + referencia));
    }

    /**
     * Reembolsar un pago
     */
    public Transaccion reembolsarPago(Long transaccionId) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        Transaccion transaccion = obtenerEstado(transaccionId);

        if (!Transaccion.EstadoTransaccion.COMPLETADO.equals(transaccion.getEstado())) {
            throw new RuntimeException("Solo se pueden reembolsar pagos completados");
        }

        // Crear reembolso en Stripe usando el PaymentIntent
        com.stripe.model.Refund.create(
            com.stripe.param.RefundCreateParams.builder()
                .setPaymentIntent(transaccion.getReferenciaExterna())
                .build()
        );

        transaccion.setEstado(Transaccion.EstadoTransaccion.REEMBOLSADO);
        return transaccionRepository.save(transaccion);
    }
}

