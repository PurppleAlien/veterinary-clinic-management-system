package com.veterinary.controller;

import com.veterinary.dto.CitaDto;
import com.veterinary.service.PublicBookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicBookingController {

    private final PublicBookingService bookingService;

    public PublicBookingController(PublicBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/veterinarios")
    public ResponseEntity<?> listVeterinarios() {
        return ResponseEntity.ok(bookingService.listActiveVeterinarios());
    }

    @GetMapping("/slots")
    public ResponseEntity<?> availableSlots(
            @RequestParam Long vetId,
            @RequestParam String date) {
        LocalDate d = LocalDate.parse(date);
        return ResponseEntity.ok(bookingService.availableSlots(vetId, d));
    }

    @PostMapping("/citas")
    public ResponseEntity<?> createCita(@RequestBody Map<String, String> body) {
        Long vetId = Long.parseLong(body.get("veterinarioId"));
        Long mascotaId = Long.parseLong(body.get("mascotaId"));
        String clienteEmail = body.get("clienteEmail");
        String motivo = body.get("motivo");

        LocalDate date = LocalDate.parse(body.get("fecha"));
        LocalTime time = LocalTime.parse(body.get("hora"));
        int duracionMinutos = 30;

        LocalDateTime inicio = LocalDateTime.of(date, time);
        LocalDateTime fin = inicio.plusMinutes(duracionMinutos);

        CitaDto dto = bookingService.crearCitaPublica(vetId, mascotaId, clienteEmail, inicio, fin, motivo);
        return ResponseEntity.ok(Map.of(
                "message", "Cita agendada exitosamente",
                "cita", dto
        ));
    }
}
