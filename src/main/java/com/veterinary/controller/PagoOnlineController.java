package com.veterinary.controller;

import com.veterinary.service.PagoOnlineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
public class PagoOnlineController {

    private final PagoOnlineService pagoOnlineService;

    public PagoOnlineController(PagoOnlineService pagoOnlineService) {
        this.pagoOnlineService = pagoOnlineService;
    }

    @PostMapping("/stripe/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Long> body) {
        Long facturaId = body.get("facturaId");
        try {
            Map<String, Object> result = pagoOnlineService.crearIntencionPago(facturaId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody String payload,
                                            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            com.stripe.model.Event event = com.stripe.net.Webhook.constructEvent(
                    payload, sigHeader, System.getenv("STRIPE_WEBHOOK_SECRET")
            );

            if ("payment_intent.succeeded".equals(event.getType())) {
                com.stripe.model.PaymentIntent intent = (com.stripe.model.PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElseThrow(() -> new RuntimeException("Invalid event"));
                String facturaId = intent.getMetadata().get("factura_id");
                pagoOnlineService.confirmarPagoWebhook(facturaId, "stripe", intent.getId());
            }

            return ResponseEntity.ok(Map.of("received", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
