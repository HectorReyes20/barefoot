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

@Slf4j
@Service
@Transactional
public class PagoManualService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    /**
     * Registrar pago manual (Yape, Plin, Transferencia)
     */
    public Transaccion registrarPagoManual(Pedido pedido, String metodoPago, String numeroOperacion) {

        Transaccion transaccion = new Transaccion();
        transaccion.setPedido(pedido);
        transaccion.setMonto(pedido.getTotal());
        transaccion.setEstado(Transaccion.EstadoTransaccion.PENDIENTE);

        // Determinar la pasarela según el método
        switch (metodoPago) {
            case "YAPE":
                transaccion.setPasarela(Transaccion.Pasarela.YAPE);
                break;
            case "PLIN":
                transaccion.setPasarela(Transaccion.Pasarela.PLIN);
                break;
            case "TRANSFERENCIA":
                transaccion.setPasarela(Transaccion.Pasarela.TRANSFERENCIA);
                break;
            case "CONTRAENTREGA":
                transaccion.setPasarela(Transaccion.Pasarela.CONTRAENTREGA);
                // Contra entrega se marca como completado automáticamente
                transaccion.setEstado(Transaccion.EstadoTransaccion.COMPLETADO);
                transaccion.setFechaConfirmacion(LocalDateTime.now());
                break;
            default:
                throw new RuntimeException("Método de pago no soportado");
        }

        transaccion.setReferenciaExterna(numeroOperacion);

        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        log.info("Pago manual registrado - Pedido: {}, Método: {}, Operación: {}",
                pedido.getNumeroPedido(), metodoPago, numeroOperacion);

        return transaccionGuardada;
    }

    /**
     * Confirmar pago manual (para admin)
     */
    public Transaccion confirmarPagoManual(Long transaccionId) {
        Transaccion transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        if (transaccion.getEstado() != Transaccion.EstadoTransaccion.PENDIENTE) {
            throw new RuntimeException("Esta transacción ya fue procesada");
        }

        transaccion.setEstado(Transaccion.EstadoTransaccion.COMPLETADO);
        transaccion.setFechaConfirmacion(LocalDateTime.now());

        // Actualizar estado del pedido
        Pedido pedido = transaccion.getPedido();
        pedido.setEstado(Pedido.EstadoPedido.CONFIRMADO);

        return transaccionRepository.save(transaccion);
    }

    /**
     * Rechazar pago manual
     */
    public Transaccion rechazarPagoManual(Long transaccionId, String motivo) {
        Transaccion transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        transaccion.setEstado(Transaccion.EstadoTransaccion.FALLIDO);
        transaccion.setMessageError(motivo);

        // Cancelar el pedido
        Pedido pedido = transaccion.getPedido();
        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);

        return transaccionRepository.save(transaccion);
    }

    /**
     * Obtener información de pago según el método
     */
    public Map<String, String> obtenerInformacionPago(String metodoPago) {
        Map<String, String> info = new HashMap<>();

        switch (metodoPago) {
            case "YAPE":
                info.put("numero", "922 928 818");
                info.put("titular", "Barefoot Store Peru");
                info.put("qr", "/images/qr-yape.png");
                info.put("instrucciones", "1. Abre tu app Yape\n2. Escanea el código QR o ingresa el número\n3. Ingresa el monto exacto\n4. Completa el pago\n5. Ingresa el número de operación");
                break;

            case "PLIN":
                info.put("numero", "922 928 818");
                info.put("titular", "Barefoot Store Peru");
                info.put("qr", "/images/qr-plin.png");
                info.put("instrucciones", "1. Abre tu app Plin\n2. Selecciona 'Enviar Plata'\n3. Ingresa el número o escanea el QR\n4. Ingresa el monto exacto\n5. Completa el pago\n6. Ingresa el número de operación");
                break;

            case "TRANSFERENCIA":
                info.put("banco", "Banco de Crédito del Perú (BCP)");
                info.put("tipoCuenta", "Cuenta Corriente Soles");
                info.put("numeroCuenta", "194-2345678-0-90");
                info.put("cci", "002-194-002345678090-15");
                info.put("titular", "Barefoot Store SAC");
                info.put("ruc", "20123456789");
                info.put("instrucciones", "1. Realiza la transferencia desde tu banca móvil o web\n2. Usa el número de cuenta o CCI\n3. Ingresa el monto exacto\n4. Guarda el comprobante\n5. Ingresa el número de operación");
                break;

            case "CONTRAENTREGA":
                info.put("instrucciones", "Pagarás en efectivo al recibir tu pedido.\n\n" +
                        "Importante:\n" +
                        "- Ten el monto exacto preparado\n" +
                        "- El delivery llevará tu pedido y la factura\n" +
                        "- Revisa tu pedido antes de pagar\n" +
                        "- Disponible solo en Lima Metropolitana");
                break;

            default:
                info.put("error", "Método de pago no disponible");
        }

        return info;
    }
}