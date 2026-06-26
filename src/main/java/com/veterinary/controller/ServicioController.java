package com.veterinary.controller;

import com.veterinary.domain.Servicio;
import com.veterinary.service.AuditService;
import com.veterinary.service.ServicioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/medico/servicios")
public class ServicioController {

    private final ServicioService service;
    private final AuditService auditService;

    public ServicioController(ServicioService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<Servicio> create(@RequestBody Servicio s, Authentication auth) {
        Servicio saved = service.create(s);
        auditService.log("CREATE", "Servicio", saved.getId(), "Creación de servicio: " + saved.getNombre(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servicio> update(@PathVariable Long id, @RequestBody Servicio s) {
        return ResponseEntity.ok(service.update(id, s));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Servicio desactivado"));
    }
}
