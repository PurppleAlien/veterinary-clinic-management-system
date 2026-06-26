package com.veterinary.controller;

import com.veterinary.domain.HistorialMedico;
import com.veterinary.service.AuditService;
import com.veterinary.service.HistorialMedicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medico/historial")
public class HistorialMedicoController {

    private final HistorialMedicoService service;
    private final AuditService auditService;

    public HistorialMedicoController(HistorialMedicoService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<HistorialMedico>> byMascota(@PathVariable Long mascotaId) {
        return ResponseEntity.ok(service.findByMascota(mascotaId));
    }

    @GetMapping("/cita/{citaId}")
    public ResponseEntity<List<HistorialMedico>> byCita(@PathVariable Long citaId) {
        return ResponseEntity.ok(service.findByCita(citaId));
    }

    @PostMapping
    public ResponseEntity<HistorialMedico> create(@RequestBody HistorialMedico h, Authentication auth) {
        HistorialMedico saved = service.create(h, (Long) auth.getCredentials());
        auditService.log("CREATE", "HistorialMedico", saved.getId(),
                "Registro de historial médico para mascota ID: " + h.getMascota().getId(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialMedico> update(@PathVariable Long id, @RequestBody HistorialMedico h) {
        return ResponseEntity.ok(service.update(id, h));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Historial eliminado"));
    }
}
