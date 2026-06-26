package com.veterinary.controller;

import com.veterinary.domain.Cliente;
import com.veterinary.repository.ClienteRepository;
import com.veterinary.repository.HistorialMedicoRepository;
import com.veterinary.service.CitaService;
import com.veterinary.service.FacturaService;
import com.veterinary.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/portal")
public class PortalClienteController {

    private final ClienteRepository clienteRepo;
    private final CitaService citaService;
    private final FacturaService facturaService;
    private final PdfService pdfService;
    private final HistorialMedicoRepository historialRepo;

    public PortalClienteController(ClienteRepository clienteRepo, CitaService citaService,
                                    FacturaService facturaService, PdfService pdfService,
                                    HistorialMedicoRepository historialRepo) {
        this.clienteRepo = clienteRepo;
        this.citaService = citaService;
        this.facturaService = facturaService;
        this.pdfService = pdfService;
        this.historialRepo = historialRepo;
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        Cliente c = clienteRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return ResponseEntity.ok(c);
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> updatePerfil(@RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        Cliente c = clienteRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        if (body.containsKey("telefono")) c.setTelefono(body.get("telefono"));
        if (body.containsKey("direccion")) c.setDireccion(body.get("direccion"));
        return ResponseEntity.ok(clienteRepo.save(c));
    }

    @GetMapping("/citas")
    public ResponseEntity<?> citas(Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return ResponseEntity.ok(citaService.findByCliente(userId));
    }

    @GetMapping("/facturas")
    public ResponseEntity<?> facturas(Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return ResponseEntity.ok(facturaService.findByCliente(userId));
    }

    @GetMapping("/historial/{mascotaId}")
    public ResponseEntity<?> historial(@PathVariable Long mascotaId) {
        return ResponseEntity.ok(historialRepo.findByMascotaIdOrderByFechaConsultaDesc(mascotaId));
    }

    @GetMapping("/facturas/{id}/pdf")
    public ResponseEntity<byte[]> facturaPdf(@PathVariable Long id) {
        byte[] pdf = pdfService.generateInvoicePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=factura-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
