package com.veterinary.controller;

import com.veterinary.dto.CitaDto;
import com.veterinary.service.AuditService;
import com.veterinary.service.CitaService;
import com.veterinary.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medico/citas")
public class CitaController {

    private final CitaService service;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuditService auditService;
    private final NotificacionService notificacionService;

    public CitaController(CitaService service, SimpMessagingTemplate messagingTemplate, AuditService auditService,
                           NotificacionService notificacionService) {
        this.service = service;
        this.messagingTemplate = messagingTemplate;
        this.auditService = auditService;
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public ResponseEntity<List<CitaDto>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitaDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/veterinario/{veterinarioId}")
    public ResponseEntity<List<CitaDto>> byVeterinario(@PathVariable Long veterinarioId) {
        return ResponseEntity.ok(service.findByVeterinario(veterinarioId));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CitaDto>> byCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.findByCliente(clienteId));
    }

    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<CitaDto>> byMascota(@PathVariable Long mascotaId) {
        return ResponseEntity.ok(service.findByMascota(mascotaId));
    }

    @PostMapping
    public ResponseEntity<CitaDto> create(@RequestBody CitaDto dto, Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        CitaDto created = service.crearCita(dto, userId);
        messagingTemplate.convertAndSend("/topic/citas", Map.of("action", "CREATED", "data", created));
        auditService.log("CREATE", "Cita", created.getId(), "Creación de cita para mascota ID: " + dto.getMascotaId(),
                userId, auth.getName());
        try { notificacionService.recordatorioCita(created.getId()); } catch (Exception e) { /* email fallback */ }
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CitaDto> update(@PathVariable Long id, @RequestBody CitaDto dto, Authentication auth) {
        CitaDto updated = service.updateCita(id, dto);
        messagingTemplate.convertAndSend("/topic/citas", Map.of("action", "UPDATED", "data", updated));
        auditService.log("UPDATE", "Cita", id, "Actualización de cita: " + dto.getEstado(),
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        service.deleteCita(id);
        messagingTemplate.convertAndSend("/topic/citas", Map.of("action", "DELETED", "data", Map.of("id", id)));
        auditService.log("DELETE", "Cita", id, "Eliminación de cita",
                (Long) auth.getCredentials(), auth.getName());
        return ResponseEntity.ok(Map.of("message", "Cita eliminada"));
    }
}
