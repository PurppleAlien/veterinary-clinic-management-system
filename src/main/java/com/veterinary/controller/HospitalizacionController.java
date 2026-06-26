package com.veterinary.controller;

import com.veterinary.domain.Hospitalizacion;
import com.veterinary.service.AuditService;
import com.veterinary.service.HospitalizacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medico/hospitalizaciones")
public class HospitalizacionController {

    private final HospitalizacionService service;
    private final AuditService auditService;

    public HospitalizacionController(HospitalizacionService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Hospitalizacion>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hospitalizacion> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Hospitalizacion> create(@RequestBody Hospitalizacion h, Authentication auth) {
        Hospitalizacion saved = service.create(h, (Long) auth.getCredentials());
        auditService.log("CREATE", "Hospitalizacion", saved.getId(),
                "Hospitalización de mascota ID: " + h.getMascota().getId(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/alta")
    public ResponseEntity<Hospitalizacion> darAlta(@PathVariable Long id, Authentication auth) {
        Hospitalizacion h = service.darAlta(id);
        auditService.log("DISCHARGE", "Hospitalizacion", id,
                "Alta de hospitalización para mascota ID: " + h.getMascota().getId(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(h);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Hospitalización eliminada"));
    }
}
