package com.veterinary.service;

import com.veterinary.domain.Factura;
import com.veterinary.domain.Pago;
import com.veterinary.domain.enums.EstadoFactura;
import com.veterinary.domain.enums.MetodoPago;
import com.veterinary.repository.FacturaRepository;
import com.veterinary.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PagoOnlineService {

    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final String stripeApiKey;

    public PagoOnlineService(FacturaRepository facturaRepository, PagoRepository pagoRepository,
                              @Value("${stripe.api-key}") String stripeApiKey) {
        this.facturaRepository = facturaRepository;
        this.pagoRepository = pagoRepository;
        this.stripeApiKey = stripeApiKey;
    }

    public Map<String, Object> crearIntencionPago(Long facturaId) {
        Factura f = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        if (f.getEstado() == EstadoFactura.PAGADA) {
            throw new RuntimeException("La factura ya está pagada");
        }

        if (stripeApiKey == null || stripeApiKey.isBlank() || stripeApiKey.contains("placeholder")) {
            return simulatePayment(f);
        }

        return stripePaymentIntent(f);
    }

    private Map<String, Object> simulatePayment(Factura f) {
        return Map.of(
                "mode", "simulado",
                "facturaId", f.getId(),
                "numeroFactura", f.getNumeroFactura(),
                "total", f.getTotal(),
                "saldoPendiente", f.getTotal() - f.getTotalPagado(),
                "moneda", "MXN",
                "mensaje", "Pago simulado (Stripe no configurado). Usa STRIPE_API_KEY para activar pagos reales."
        );
    }

    private Map<String, Object> stripePaymentIntent(Factura f) {
        try {
            com.stripe.Stripe.apiKey = stripeApiKey;
            double montoPendiente = f.getTotal() - f.getTotalPagado();
            int montoCentavos = (int) Math.round(montoPendiente * 100);

            com.stripe.model.PaymentIntent intent = com.stripe.model.PaymentIntent.create(
                    com.stripe.param.PaymentIntentCreateParams.builder()
                            .setAmount((long) montoCentavos)
                            .setCurrency("mxn")
                            .putMetadata("factura_id", f.getId().toString())
                            .putMetadata("numero_factura", f.getNumeroFactura())
                            .build()
            );

            return Map.of(
                    "mode", "stripe",
                    "clientSecret", intent.getClientSecret(),
                    "facturaId", f.getId(),
                    "numeroFactura", f.getNumeroFactura(),
                    "total", f.getTotal(),
                    "moneda", "MXN"
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al crear pago con Stripe: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void confirmarPagoWebhook(String facturaIdStr, String metodo, String referencia) {
        Long facturaId = Long.parseLong(facturaIdStr);
        Factura f = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        Pago pago = new Pago();
        pago.setFactura(f);
        pago.setMonto(f.getTotal() - f.getTotalPagado());
        pago.setFechaPago(LocalDateTime.now());
        pago.setMetodo(MetodoPago.TARJETA_CREDITO);
        pago.setObservacion("Pago online - " + metodo);
        pago.setReferencia(referencia);
        pagoRepository.save(pago);

        f.setTotalPagado(f.getTotal());
        f.setEstado(EstadoFactura.PAGADA);
        f.setFormaPago(MetodoPago.TARJETA_CREDITO);
        facturaRepository.save(f);
    }
}
