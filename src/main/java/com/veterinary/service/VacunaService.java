package com.veterinary.service;

import com.veterinary.domain.Vacuna;
import com.veterinary.repository.MascotaRepository;
import com.veterinary.repository.VacunaRepository;
import com.veterinary.repository.VeterinarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VacunaService {

    private final VacunaRepository repository;
    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;

    public VacunaService(VacunaRepository repository, MascotaRepository mascotaRepository,
                          VeterinarioRepository veterinarioRepository) {
        this.repository = repository;
        this.mascotaRepository = mascotaRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    public List<Vacuna> findByMascota(Long mascotaId) {
        return repository.findByMascotaIdOrderByFechaAplicacionDesc(mascotaId);
    }

    @Transactional
    public Vacuna create(Vacuna v, Long vetId) {
        v.setMascota(mascotaRepository.findById(v.getMascota().getId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada")));
        v.setVeterinario(veterinarioRepository.findById(vetId)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado")));
        return repository.save(v);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
