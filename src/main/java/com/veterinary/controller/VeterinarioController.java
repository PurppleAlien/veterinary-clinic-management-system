package com.veterinary.controller;

import com.veterinary.domain.Veterinario;
import com.veterinary.service.AuditService;
import com.veterinary.service.VeterinarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/medico")
public class VeterinarioController {

    private final VeterinarioService service;
    private final AuditService auditService;

    public VeterinarioController(VeterinarioService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping("/veterinarios")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/veterinarios/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping("/veterinarios")
    public ResponseEntity<?> create(@RequestBody Veterinario v, Authentication auth) {
        Veterinario saved = service.create(v);
        auditService.log("CREATE", "Veterinario", saved.getId(), "Creación de veterinario: " + saved.getEmail(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/veterinarios/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Veterinario v, Authentication auth) {
        Veterinario updated = service.update(id, v);
        auditService.log("UPDATE", "Veterinario", id, "Actualización de veterinario: " + updated.getEmail(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/veterinarios/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        service.delete(id);
        auditService.log("DELETE", "Veterinario", id, "Desactivación de veterinario",
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(Map.of("message", "Veterinario desactivado"));
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        Veterinario v = service.updateProfile(userId, body);
        auditService.log("UPDATE_PROFILE", "Veterinario", userId, "Actualización de perfil",
                userId, auth.getName());
        return ResponseEntity.ok(v);
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        service.changePassword(userId, body.get("passwordActual"), body.get("passwordNueva"));
        auditService.log("CHANGE_PASSWORD", "Veterinario", userId, "Cambio de contraseña",
                userId, auth.getName());
        return ResponseEntity.ok(Map.of("message", "Contraseña cambiada exitosamente"));
    }
}
