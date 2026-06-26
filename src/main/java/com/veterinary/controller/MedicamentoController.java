package com.veterinary.controller;

import com.veterinary.domain.Medicamento;
import com.veterinary.service.AuditService;
import com.veterinary.service.MedicamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/medico/medicamentos")
public class MedicamentoController {

    private final MedicamentoService service;
    private final AuditService auditService;

    public MedicamentoController(MedicamentoService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<Medicamento> create(@RequestBody Medicamento m, Authentication auth) {
        Medicamento saved = service.create(m);
        auditService.log("CREATE", "Medicamento", saved.getId(), "Creación de medicamento: " + saved.getNombre(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medicamento> update(@PathVariable Long id, @RequestBody Medicamento m) {
        return ResponseEntity.ok(service.update(id, m));
    }

    @PostMapping("/{id}/ajustar-stock")
    public ResponseEntity<Medicamento> adjustStock(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                    Authentication auth) {
        Medicamento m = service.adjustStock(id, body);
        auditService.log("STOCK_ADJUST", "Medicamento", id,
                "Ajuste de stock: " + body.get("tipo") + " " + body.get("cantidad") + " (" + m.getNombre() + ")",
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(m);
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<?> stockBajo() {
        return ResponseEntity.ok(service.stockBajo());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Medicamento desactivado"));
    }
}
