package com.veterinary.service;

import com.veterinary.domain.Mascota;
import com.veterinary.dto.MascotaDto;
import com.veterinary.repository.ClienteRepository;
import com.veterinary.repository.MascotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MascotaService {

    private final MascotaRepository repository;
    private final ClienteRepository clienteRepository;

    public MascotaService(MascotaRepository repository, ClienteRepository clienteRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
    }

    public List<Mascota> findAll() {
        return repository.findAll();
    }

    public MascotaDto findById(Long id) {
        Mascota m = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        return toDto(m);
    }

    public List<Mascota> findByCliente(Long clienteId) {
        return repository.findByClienteIdOrderByNombreAsc(clienteId);
    }

    @Transactional
    public Mascota create(Mascota m) {
        if (m.getCliente() == null || m.getCliente().getId() == null) {
            throw new RuntimeException("El cliente es requerido");
        }
        m.setCliente(clienteRepository.findById(m.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado")));
        return repository.save(m);
    }

    @Transactional
    public Mascota update(Long id, Mascota m) {
        Mascota existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        if (m.getNombre() != null) existing.setNombre(m.getNombre());
        if (m.getEspecie() != null) existing.setEspecie(m.getEspecie());
        if (m.getRaza() != null) existing.setRaza(m.getRaza());
        if (m.getColor() != null) existing.setColor(m.getColor());
        if (m.getGenero() != null) existing.setGenero(m.getGenero());
        if (m.getFechaNacimiento() != null) existing.setFechaNacimiento(m.getFechaNacimiento());
        if (m.getPeso() != null) existing.setPeso(m.getPeso());
        if (m.getAlergias() != null) existing.setAlergias(m.getAlergias());
        if (m.getCondicionesMedicas() != null) existing.setCondicionesMedicas(m.getCondicionesMedicas());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public MascotaDto toDto(Mascota m) {
        MascotaDto dto = new MascotaDto();
        dto.setId(m.getId());
        dto.setNombre(m.getNombre());
        dto.setEspecie(m.getEspecie().name());
        dto.setRaza(m.getRaza());
        dto.setColor(m.getColor());
        dto.setGenero(m.getGenero() != null ? m.getGenero().name() : null);
        dto.setFechaNacimiento(m.getFechaNacimiento());
        dto.setPeso(m.getPeso());
        dto.setAlergias(m.getAlergias());
        dto.setCondicionesMedicas(m.getCondicionesMedicas());
        dto.setClienteId(m.getCliente().getId());
        dto.setClienteNombre(m.getCliente().getNombre() + " " + m.getCliente().getApellido());
        return dto;
    }
}
