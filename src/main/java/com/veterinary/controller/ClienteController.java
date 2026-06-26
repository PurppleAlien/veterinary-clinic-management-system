package com.veterinary.controller;

import com.veterinary.domain.Cliente;
import com.veterinary.dto.ClienteDto;
import com.veterinary.service.AuditService;
import com.veterinary.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medico/clientes")
public class ClienteController {

    private final ClienteService service;
    private final AuditService auditService;

    public ClienteController(ClienteService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> list(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(service.findAll(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Cliente> create(@RequestBody Cliente c, Authentication auth) {
        Cliente saved = service.create(c);
        auditService.log("CREATE", "Cliente", saved.getId(), "Creación de cliente: " + saved.getEmail(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> update(@PathVariable Long id, @RequestBody Cliente c) {
        return ResponseEntity.ok(service.update(id, c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Cliente eliminado"));
    }
}
