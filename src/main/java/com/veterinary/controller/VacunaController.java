package com.veterinary.controller;

import com.veterinary.domain.Vacuna;
import com.veterinary.service.AuditService;
import com.veterinary.service.VacunaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medico/vacunas")
public class VacunaController {

    private final VacunaService service;
    private final AuditService auditService;

    public VacunaController(VacunaService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<Vacuna>> byMascota(@PathVariable Long mascotaId) {
        return ResponseEntity.ok(service.findByMascota(mascotaId));
    }

    @PostMapping
    public ResponseEntity<Vacuna> create(@RequestBody Vacuna v, Authentication auth) {
        Vacuna saved = service.create(v, (Long) auth.getCredentials());
        auditService.log("CREATE", "Vacuna", saved.getId(),
                "Vacunación: " + v.getNombre() + " para mascota ID: " + v.getMascota().getId(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Vacuna eliminada"));
    }
}
