package com.veterinary.service;

import com.veterinary.domain.PasoTratamiento;
import com.veterinary.domain.PlanTratamiento;
import com.veterinary.domain.enums.EstadoPaso;
import com.veterinary.repository.MascotaRepository;
import com.veterinary.repository.PasoTratamientoRepository;
import com.veterinary.repository.PlanTratamientoRepository;
import com.veterinary.repository.VeterinarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PlanTratamientoService {

    private final PlanTratamientoRepository repository;
    private final PasoTratamientoRepository pasoRepository;
    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;

    public PlanTratamientoService(PlanTratamientoRepository repository, PasoTratamientoRepository pasoRepository,
                                   MascotaRepository mascotaRepository, VeterinarioRepository veterinarioRepository) {
        this.repository = repository;
        this.pasoRepository = pasoRepository;
        this.mascotaRepository = mascotaRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    public List<PlanTratamiento> findByMascota(Long mascotaId) {
        return repository.findByMascotaIdOrderByFechaInicioDesc(mascotaId);
    }

    public PlanTratamiento findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
    }

    @Transactional
    public PlanTratamiento create(PlanTratamiento p, Long vetId) {
        p.setMascota(mascotaRepository.findById(p.getMascota().getId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada")));
        p.setVeterinario(veterinarioRepository.findById(vetId)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado")));
        return repository.save(p);
    }

    @Transactional
    public PlanTratamiento update(Long id, PlanTratamiento p) {
        PlanTratamiento existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
        if (p.getTitulo() != null) existing.setTitulo(p.getTitulo());
        if (p.getDescripcion() != null) existing.setDescripcion(p.getDescripcion());
        if (p.getEstado() != null) existing.setEstado(p.getEstado());
        if (p.getCostoEstimado() != null) existing.setCostoEstimado(p.getCostoEstimado());
        return repository.save(existing);
    }

    @Transactional
    public PasoTratamiento addPaso(Long planId, PasoTratamiento paso) {
        PlanTratamiento plan = repository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
        paso.setPlanTratamiento(plan);
        return pasoRepository.save(paso);
    }

    @Transactional
    public PasoTratamiento updatePaso(Long id, PasoTratamiento p) {
        PasoTratamiento existing = pasoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paso no encontrado"));
        if (p.getDescripcion() != null) existing.setDescripcion(p.getDescripcion());
        if (p.getEstado() != null) existing.setEstado(p.getEstado());
        if (p.getNotas() != null) existing.setNotas(p.getNotas());
        return pasoRepository.save(existing);
    }

    @Transactional
    public PasoTratamiento updatePasoEstado(Long id, String estado) {
        PasoTratamiento paso = pasoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paso no encontrado"));
        paso.setEstado(EstadoPaso.valueOf(estado));
        return pasoRepository.save(paso);
    }

    @Transactional
    public void deletePaso(Long id) {
        pasoRepository.deleteById(id);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
