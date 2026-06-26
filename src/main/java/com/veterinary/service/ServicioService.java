package com.veterinary.service;

import com.veterinary.domain.Servicio;
import com.veterinary.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioService {

    private final ServicioRepository repository;

    public ServicioService(ServicioRepository repository) {
        this.repository = repository;
    }

    public List<Servicio> findAll() {
        return repository.findByActivoTrue();
    }

    @Transactional
    public Servicio create(Servicio s) {
        s.setActivo(true);
        return repository.save(s);
    }

    @Transactional
    public Servicio update(Long id, Servicio s) {
        Servicio existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        if (s.getNombre() != null) existing.setNombre(s.getNombre());
        if (s.getDescripcion() != null) existing.setDescripcion(s.getDescripcion());
        if (s.getPrecioBase() != null) existing.setPrecioBase(s.getPrecioBase());
        if (s.getCodigoInterno() != null) existing.setCodigoInterno(s.getCodigoInterno());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Servicio s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        s.setActivo(false);
        repository.save(s);
    }
}
