package com.veterinary.controller;

import com.veterinary.domain.ConsentimientoInformado;
import com.veterinary.service.AuditService;
import com.veterinary.service.ConsentimientoService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medico/consentimientos")
public class ConsentimientoController {

    private final ConsentimientoService service;
    private final AuditService auditService;

    public ConsentimientoController(ConsentimientoService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<ConsentimientoInformado>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ConsentimientoInformado>> byCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.findByCliente(clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsentimientoInformado> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ConsentimientoInformado> create(@RequestBody ConsentimientoInformado c, Authentication auth) {
        ConsentimientoInformado saved = service.create(c, (Long) auth.getCredentials());
        auditService.log("CREATE", "Consentimiento", saved.getId(),
                "Consentimiento: " + c.getTitulo(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsentimientoInformado> update(@PathVariable Long id, @RequestBody ConsentimientoInformado c) {
        return ResponseEntity.ok(service.update(id, c));
    }

    @PatchMapping("/{id}/firmar")
    public ResponseEntity<ConsentimientoInformado> sign(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.sign(id, body.get("nombreFirmante")));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        ConsentimientoInformado c = service.findById(id);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));
        Document document = new Document(pdfDoc);
        document.add(new Paragraph("CONSENTIMIENTO INFORMADO").setBold().setFontSize(18));
        document.add(new Paragraph("Título: " + c.getTitulo()));
        document.add(new Paragraph("Procedimiento: " + c.getTipoProcedimiento()));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(c.getContenido()));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Firmado: " + (c.getFirmado() ? "SÍ" : "NO")));
        if (c.getFirmado()) {
            document.add(new Paragraph("Firmante: " + c.getNombreFirmante()));
            document.add(new Paragraph("Fecha: " + c.getFechaFirma().toString()));
        }
        document.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=consentimiento-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Consentimiento eliminado"));
    }
}
