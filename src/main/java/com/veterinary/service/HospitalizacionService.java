package com.veterinary.service;

import com.veterinary.domain.Hospitalizacion;
import com.veterinary.domain.enums.EstadoHospitalizacion;
import com.veterinary.repository.HospitalizacionRepository;
import com.veterinary.repository.MascotaRepository;
import com.veterinary.repository.VeterinarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HospitalizacionService {

    private final HospitalizacionRepository repository;
    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;

    public HospitalizacionService(HospitalizacionRepository repository, MascotaRepository mascotaRepository,
                                   VeterinarioRepository veterinarioRepository) {
        this.repository = repository;
        this.mascotaRepository = mascotaRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    public List<Hospitalizacion> findAll() {
        return repository.findByEstado(EstadoHospitalizacion.HOSPITALIZADO);
    }

    public Hospitalizacion findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospitalización no encontrada"));
    }

    @Transactional
    public Hospitalizacion create(Hospitalizacion h, Long vetId) {
        h.setMascota(mascotaRepository.findById(h.getMascota().getId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada")));
        h.setVeterinario(veterinarioRepository.findById(vetId)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado")));
        h.setCheckIn(LocalDateTime.now());
        h.setEstado(EstadoHospitalizacion.HOSPITALIZADO);
        return repository.save(h);
    }

    @Transactional
    public Hospitalizacion darAlta(Long id) {
        Hospitalizacion h = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospitalización no encontrada"));
        h.setCheckOut(LocalDateTime.now());
        h.setEstado(EstadoHospitalizacion.DADO_ALTA);
        return repository.save(h);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
