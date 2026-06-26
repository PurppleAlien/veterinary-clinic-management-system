package com.veterinary.controller;

import com.veterinary.domain.Mascota;
import com.veterinary.dto.MascotaDto;
import com.veterinary.service.AuditService;
import com.veterinary.service.MascotaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medico/mascotas")
public class MascotaController {

    private final MascotaService service;
    private final AuditService auditService;

    public MascotaController(MascotaService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Mascota>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MascotaDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Mascota>> findByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.findByCliente(clienteId));
    }

    @PostMapping
    public ResponseEntity<Mascota> create(@RequestBody Mascota m, Authentication auth) {
        Mascota saved = service.create(m);
        auditService.log("CREATE", "Mascota", saved.getId(), "Creación de mascota: " + saved.getNombre(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> update(@PathVariable Long id, @RequestBody Mascota m) {
        return ResponseEntity.ok(service.update(id, m));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Mascota eliminada"));
    }
}
