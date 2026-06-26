package com.veterinary.service;

import com.veterinary.domain.*;
import com.veterinary.domain.enums.EstadoCita;
import com.veterinary.dto.CitaDto;
import com.veterinary.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final MascotaRepository mascotaRepository;
    private final ClienteRepository clienteRepository;

    public CitaService(CitaRepository citaRepository, VeterinarioRepository veterinarioRepository,
                       MascotaRepository mascotaRepository, ClienteRepository clienteRepository) {
        this.citaRepository = citaRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.clienteRepository = clienteRepository;
    }

    public CitaDto toDto(Cita c) {
        CitaDto dto = new CitaDto();
        dto.setId(c.getId());
        dto.setMascotaId(c.getMascota().getId());
        dto.setMascotaNombre(c.getMascota().getNombre());
        dto.setEspecie(c.getMascota().getEspecie().name());
        dto.setClienteId(c.getCliente().getId());
        dto.setClienteNombre(c.getCliente().getNombre() + " " + c.getCliente().getApellido());
        dto.setVeterinarioId(c.getVeterinario().getId());
        dto.setVeterinarioNombre(c.getVeterinario().getNombre() + " " + c.getVeterinario().getApellido());
        dto.setFechaHoraInicio(c.getFechaHoraInicio());
        dto.setFechaHoraFin(c.getFechaHoraFin());
        dto.setMotivo(c.getMotivo());
        dto.setNotas(c.getNotas());
        dto.setEstado(c.getEstado().name());
        dto.setMotivoCancelacion(c.getMotivoCancelacion());
        return dto;
    }

    public List<CitaDto> findAll() {
        return citaRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<CitaDto> findByVeterinario(Long veterinarioId) {
        return citaRepository.findByVeterinarioIdOrderByFechaHoraInicioDesc(veterinarioId)
                .stream().map(this::toDto).toList();
    }

    public List<CitaDto> findByCliente(Long clienteId) {
        return citaRepository.findByClienteIdOrderByFechaHoraInicioDesc(clienteId)
                .stream().map(this::toDto).toList();
    }

    public List<CitaDto> findByMascota(Long mascotaId) {
        return citaRepository.findByMascotaIdOrderByFechaHoraInicioDesc(mascotaId)
                .stream().map(this::toDto).toList();
    }

    public CitaDto findById(Long id) {
        Cita c = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        return toDto(c);
    }

    @Transactional
    public CitaDto crearCita(CitaDto dto, Long veterinarioId) {
        Mascota mascota = mascotaRepository.findById(dto.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        Veterinario vet = veterinarioRepository.findById(veterinarioId)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        if (dto.getFechaHoraInicio() == null || dto.getFechaHoraFin() == null) {
            throw new RuntimeException("La fecha de inicio y fin son requeridas");
        }

        List<Cita> ocupadas = citaRepository.findCitasOcupadas(
                veterinarioId, dto.getFechaHoraInicio(), dto.getFechaHoraFin());
        if (!ocupadas.isEmpty()) {
            throw new RuntimeException("El veterinario ya tiene una cita en ese horario");
        }

        Cita cita = new Cita();
        cita.setMascota(mascota);
        cita.setVeterinario(vet);
        cita.setCliente(mascota.getCliente());
        cita.setFechaHoraInicio(dto.getFechaHoraInicio());
        cita.setFechaHoraFin(dto.getFechaHoraFin());
        cita.setMotivo(dto.getMotivo());
        cita.setNotas(dto.getNotas());
        cita.setEstado(dto.getEstado() != null ? EstadoCita.valueOf(dto.getEstado()) : EstadoCita.PROGRAMADA);

        return toDto(citaRepository.save(cita));
    }

    @Transactional
    public CitaDto updateCita(Long id, CitaDto dto) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (dto.getFechaHoraInicio() != null) cita.setFechaHoraInicio(dto.getFechaHoraInicio());
        if (dto.getFechaHoraFin() != null) cita.setFechaHoraFin(dto.getFechaHoraFin());
        if (dto.getMotivo() != null) cita.setMotivo(dto.getMotivo());
        if (dto.getNotas() != null) cita.setNotas(dto.getNotas());
        if (dto.getEstado() != null) cita.setEstado(EstadoCita.valueOf(dto.getEstado()));
        if (dto.getMotivoCancelacion() != null) cita.setMotivoCancelacion(dto.getMotivoCancelacion());

        return toDto(citaRepository.save(cita));
    }

    @Transactional
    public void deleteCita(Long id) {
        citaRepository.deleteById(id);
    }
}
