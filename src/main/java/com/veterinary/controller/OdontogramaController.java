package com.veterinary.controller;

import com.veterinary.domain.Odontograma;
import com.veterinary.service.OdontogramaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medico")
public class OdontogramaController {

    private final OdontogramaService service;

    public OdontogramaController(OdontogramaService service) {
        this.service = service;
    }

    @GetMapping("/mascotas/{mascotaId}/odontogramas")
    public ResponseEntity<List<Odontograma>> list(@PathVariable Long mascotaId) {
        return ResponseEntity.ok(service.findByMascota(mascotaId));
    }

    @GetMapping("/odontogramas/{id}")
    public ResponseEntity<Odontograma> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping("/mascotas/{mascotaId}/odontogramas")
    public ResponseEntity<Odontograma> create(@PathVariable Long mascotaId,
                                               @RequestBody Map<String, Object> body,
                                               Authentication auth) {
        String vetName = auth != null ? auth.getName() : "Veterinario";
        return ResponseEntity.ok(service.create(mascotaId, body, vetName));
    }

    @PutMapping("/odontogramas/{id}")
    public ResponseEntity<Odontograma> update(@PathVariable Long id,
                                               @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.update(id, body));
    }

    @DeleteMapping("/odontogramas/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Odontograma eliminado"));
    }
}
