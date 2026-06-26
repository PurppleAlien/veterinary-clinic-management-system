package com.veterinary.service;

import com.veterinary.domain.HistorialMedico;
import com.veterinary.repository.CitaRepository;
import com.veterinary.repository.HistorialMedicoRepository;
import com.veterinary.repository.MascotaRepository;
import com.veterinary.repository.VeterinarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistorialMedicoService {

    private final HistorialMedicoRepository repository;
    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;

    public HistorialMedicoService(HistorialMedicoRepository repository, CitaRepository citaRepository,
                                   MascotaRepository mascotaRepository, VeterinarioRepository veterinarioRepository) {
        this.repository = repository;
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    public List<HistorialMedico> findByMascota(Long mascotaId) {
        return repository.findByMascotaIdOrderByFechaConsultaDesc(mascotaId);
    }

    public List<HistorialMedico> findByCita(Long citaId) {
        return repository.findByCitaId(citaId);
    }

    @Transactional
    public HistorialMedico create(HistorialMedico h, Long vetId) {
        h.setMascota(mascotaRepository.findById(h.getMascota().getId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada")));
        h.setVeterinario(veterinarioRepository.findById(vetId)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado")));
        if (h.getCita() != null && h.getCita().getId() != null) {
            h.setCita(citaRepository.findById(h.getCita().getId()).orElse(null));
        }
        return repository.save(h);
    }

    @Transactional
    public HistorialMedico update(Long id, HistorialMedico h) {
        HistorialMedico existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Historial no encontrado"));
        if (h.getMotivo() != null) existing.setMotivo(h.getMotivo());
        if (h.getDiagnostico() != null) existing.setDiagnostico(h.getDiagnostico());
        if (h.getProcedimientoRealizado() != null) existing.setProcedimientoRealizado(h.getProcedimientoRealizado());
        if (h.getMedicacionIndicada() != null) existing.setMedicacionIndicada(h.getMedicacionIndicada());
        if (h.getObservaciones() != null) existing.setObservaciones(h.getObservaciones());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
