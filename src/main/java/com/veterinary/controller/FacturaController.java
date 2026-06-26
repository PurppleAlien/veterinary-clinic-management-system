package com.veterinary.controller;

import com.veterinary.dto.FacturaDto;
import com.veterinary.service.AuditService;
import com.veterinary.service.FacturaService;
import com.veterinary.service.NotificacionService;
import com.veterinary.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medico/facturas")
public class FacturaController {

    private final FacturaService service;
    private final PdfService pdfService;
    private final AuditService auditService;
    private final NotificacionService notificacionService;

    public FacturaController(FacturaService service, PdfService pdfService, AuditService auditService,
                              NotificacionService notificacionService) {
        this.service = service;
        this.pdfService = pdfService;
        this.auditService = auditService;
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public ResponseEntity<List<FacturaDto>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<FacturaDto>> byCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.findByCliente(clienteId));
    }

    @PostMapping
    public ResponseEntity<FacturaDto> create(@RequestBody FacturaDto dto, Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        FacturaDto created = service.crearFactura(dto, userId);
        auditService.log("CREATE", "Factura", created.getId(), "Creación de factura",
                userId, auth.getName());
        try { notificacionService.notificarFactura(created.getId()); } catch (Exception e) { /* email fallback */ }
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{id}/pago")
    public ResponseEntity<FacturaDto> addPayment(@PathVariable Long id,
                                                  @RequestBody FacturaDto.PagoDto pagoDto,
                                                  Authentication auth) {
        FacturaDto updated = service.registrarPago(id, pagoDto);
        auditService.log("PAYMENT", "Factura", id, "Registro de pago: $" + pagoDto.getMonto(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdf = pdfService.generateInvoicePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=factura-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("message", "Eliminación no implementada"));
    }
}
