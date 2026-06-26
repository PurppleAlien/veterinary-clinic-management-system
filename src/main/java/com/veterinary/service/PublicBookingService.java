package com.veterinary.service;

import com.veterinary.domain.*;
import com.veterinary.domain.enums.EstadoCita;
import com.veterinary.dto.CitaDto;
import com.veterinary.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PublicBookingService {

    private final VeterinarioRepository veterinarioRepository;
    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final ClienteRepository clienteRepository;

    public PublicBookingService(VeterinarioRepository veterinarioRepository, CitaRepository citaRepository,
                                 MascotaRepository mascotaRepository, ClienteRepository clienteRepository) {
        this.veterinarioRepository = veterinarioRepository;
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<Map<String, Object>> listActiveVeterinarios() {
        return veterinarioRepository.findByActivoTrue().stream()
                .map(v -> Map.<String, Object>of(
                        "id", v.getId(),
                        "nombre", v.getNombre() + " " + v.getApellido(),
                        "especialidad", v.getEspecialidad() != null ? v.getEspecialidad() : "",
                        "horarioInicio", v.getHorarioInicio() != null ? v.getHorarioInicio().toString() : "09:00",
                        "horarioFin", v.getHorarioFin() != null ? v.getHorarioFin().toString() : "18:00"
                )).toList();
    }

    public List<Map<String, Object>> availableSlots(Long vetId, LocalDate date) {
        Veterinario vet = veterinarioRepository.findById(vetId)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        LocalTime start = vet.getHorarioInicio() != null ? vet.getHorarioInicio() : LocalTime.of(9, 0);
        LocalTime end = vet.getHorarioFin() != null ? vet.getHorarioFin() : LocalTime.of(18, 0);
        int slotMinutes = vet.getDuracionTurnoMinutos() != null ? vet.getDuracionTurnoMinutos() : 30;

        List<Cita> ocupadas = citaRepository.findCitasOcupadas(
                vetId,
                LocalDateTime.of(date, start),
                LocalDateTime.of(date, end)
        );

        List<Map<String, Object>> slots = new ArrayList<>();
        LocalTime current = start;

        while (current.isBefore(end)) {
            LocalDateTime slotStart = LocalDateTime.of(date, current);
            LocalDateTime slotEnd = slotStart.plusMinutes(slotMinutes);

            boolean isOccupied = ocupadas.stream().anyMatch(c ->
                    c.getFechaHoraInicio().isBefore(slotEnd) && c.getFechaHoraFin().isAfter(slotStart));

            if (slotStart.isAfter(LocalDateTime.now())) {
                slots.add(Map.of(
                        "hora", current.toString(),
                        "disponible", !isOccupied
                ));
            }

            current = current.plusMinutes(slotMinutes);
        }

        return slots;
    }

    @Transactional
    public CitaDto crearCitaPublica(Long vetId, Long mascotaId, String clienteEmail,
                                     LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin,
                                     String motivo) {
        Veterinario vet = veterinarioRepository.findById(vetId)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
        if (!vet.getActivo()) {
            throw new RuntimeException("El veterinario no está disponible");
        }

        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));

        Cliente cliente = mascota.getCliente();
        if (clienteEmail != null && !clienteEmail.isBlank()
                && !cliente.getEmail().equalsIgnoreCase(clienteEmail)) {
            throw new RuntimeException("El email no corresponde al dueño de la mascota");
        }

        List<Cita> ocupadas = citaRepository.findCitasOcupadas(vetId, fechaHoraInicio, fechaHoraFin);
        if (!ocupadas.isEmpty()) {
            throw new RuntimeException("El horario seleccionado no está disponible");
        }

        Cita cita = new Cita();
        cita.setMascota(mascota);
        cita.setVeterinario(vet);
        cita.setCliente(cliente);
        cita.setFechaHoraInicio(fechaHoraInicio);
        cita.setFechaHoraFin(fechaHoraFin);
        cita.setMotivo(motivo != null ? motivo : "Cita agendada desde portal público");
        cita.setNotas("Agendada desde portal público");
        cita.setEstado(EstadoCita.PROGRAMADA);

        cita = citaRepository.save(cita);

        CitaService cs = new CitaService(citaRepository, veterinarioRepository, mascotaRepository, clienteRepository);
        return cs.toDto(cita);
    }
}
