package com.barefoot.service;

import com.barefoot.model.Pedido;
import com.barefoot.model.Transaccion;
import com.barefoot.repository.TransaccionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class PagoManualService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    /**
     * Registrar pago manual (Estandarizado para el Controller)
     */
    public Transaccion registrarPagoManual(Pedido pedido, String metodoPago, String codigoAprobacion) {

        // 1. Validar duplicados (EXCEPCIÓN PARA TU DEMO)
        // Si el código es el "mágico" 654321, permitimos que se repita para que hagas varias pruebas.
        // Si es otro código, validamos que sea único en la base de datos.
        if (!"654321".equals(codigoAprobacion)) {
            transaccionRepository.findByReferenciaExterna(codigoAprobacion)
                    .ifPresent(t -> {
                        throw new RuntimeException("Este código de operación ya fue registrado anteriormente");
                    });
        }

        // 2. Crear la transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setPedido(pedido);
        transaccion.setMonto(pedido.getTotal());
        transaccion.setEstado(Transaccion.EstadoTransaccion.PENDIENTE); // Nace pendiente

        // Asignamos YAPE directamente (o según el parámetro si decides expandir luego)
        if ("YAPE".equalsIgnoreCase(metodoPago)) {
            transaccion.setPasarela(Transaccion.Pasarela.YAPE);
        } else {
            throw new RuntimeException("Método de pago no soportado en manual: " + metodoPago);
        }

        transaccion.setReferenciaExterna(codigoAprobacion);

        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        log.info("Pago Manual registrado - Pedido: {}, Método: {}, Código: {}",
                pedido.getNumeroPedido(), metodoPago, codigoAprobacion);

        return transaccionGuardada;
    }

    /**
     * Confirmar pago manual (Usado por AdminPagosController)
     */
    public Transaccion confirmarPagoManual(Long transaccionId) {
        Transaccion transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (transaccion.getEstado() != Transaccion.EstadoTransaccion.PENDIENTE) {
            throw new RuntimeException("Esta transacción ya fue procesada");
        }

        // Completar transacción
        transaccion.setEstado(Transaccion.EstadoTransaccion.COMPLETADO);
        transaccion.setFechaConfirmacion(LocalDateTime.now());

        // Actualizar estado del pedido automáticamente
        Pedido pedido = transaccion.getPedido();
        pedido.setEstado(Pedido.EstadoPedido.CONFIRMADO);

        log.info("Pago confirmado manualmente - Pedido: {}", pedido.getNumeroPedido());

        return transaccionRepository.save(transaccion);
    }

    /**
     * Rechazar pago manual (Usado por AdminPagosController)
     */
    public Transaccion rechazarPagoManual(Long transaccionId, String motivo) {
        Transaccion transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        // Marcar como fallido
        transaccion.setEstado(Transaccion.EstadoTransaccion.FALLIDO);
        transaccion.setMessageError(motivo);

        // Cancelar el pedido automáticamente
        Pedido pedido = transaccion.getPedido();
        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);

        log.warn("Pago rechazado - Pedido: {}, Motivo: {}", pedido.getNumeroPedido(), motivo);

        return transaccionRepository.save(transaccion);
    }

    /**
     * Obtener información de pago (Solo YAPE)
     */
    public Map<String, String> obtenerInformacionPago(String metodoPago) {
        Map<String, String> info = new HashMap<>();

        if ("YAPE".equalsIgnoreCase(metodoPago)) {
            info.put("numero", "922 928 818"); // <--- TU NÚMERO
            info.put("titular", "Barefoot Store Peru");
            info.put("qr", "/images/qr-yape.png"); // Asegúrate de tener esta imagen
            info.put("instrucciones",
                    "1. Abre tu app Yape\n" +
                            "2. Escanea el QR o ingresa el número\n" +
                            "3. Paga el monto exacto\n" +
                            "4. Copia el 'Código de Aprobación' (6 dígitos)\n" +
                            "5. Ingrésalo aquí para validar tu compra"
            );
        } else {
            info.put("error", "Método de pago no disponible");
        }

        return info;
    }
}