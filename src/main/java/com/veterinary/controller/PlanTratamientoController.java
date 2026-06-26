package com.veterinary.controller;

import com.veterinary.domain.PasoTratamiento;
import com.veterinary.domain.PlanTratamiento;
import com.veterinary.service.AuditService;
import com.veterinary.service.PlanTratamientoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medico/planes")
public class PlanTratamientoController {

    private final PlanTratamientoService service;
    private final AuditService auditService;

    public PlanTratamientoController(PlanTratamientoService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<PlanTratamiento>> byMascota(@PathVariable Long mascotaId) {
        return ResponseEntity.ok(service.findByMascota(mascotaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanTratamiento> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<PlanTratamiento> create(@RequestBody PlanTratamiento p, Authentication auth) {
        PlanTratamiento saved = service.create(p, (Long) auth.getCredentials());
        auditService.log("CREATE", "PlanTratamiento", saved.getId(),
                "Plan de tratamiento: " + p.getTitulo(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanTratamiento> update(@PathVariable Long id, @RequestBody PlanTratamiento p) {
        return ResponseEntity.ok(service.update(id, p));
    }

    @PostMapping("/{planId}/pasos")
    public ResponseEntity<PasoTratamiento> addPaso(@PathVariable Long planId, @RequestBody PasoTratamiento paso) {
        return ResponseEntity.ok(service.addPaso(planId, paso));
    }

    @PutMapping("/pasos/{id}")
    public ResponseEntity<PasoTratamiento> updatePaso(@PathVariable Long id, @RequestBody PasoTratamiento p) {
        return ResponseEntity.ok(service.updatePaso(id, p));
    }

    @PatchMapping("/pasos/{id}/estado")
    public ResponseEntity<PasoTratamiento> updatePasoEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.updatePasoEstado(id, body.get("estado")));
    }

    @DeleteMapping("/pasos/{id}")
    public ResponseEntity<?> deletePaso(@PathVariable Long id) {
        service.deletePaso(id);
        return ResponseEntity.ok(Map.of("message", "Paso eliminado"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Plan eliminado"));
    }
}
