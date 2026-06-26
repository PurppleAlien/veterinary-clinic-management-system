package com.veterinary.service;

import com.veterinary.domain.Cliente;
import com.veterinary.dto.ClienteDto;
import com.veterinary.dto.MascotaDto;
import com.veterinary.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> findAll(String q) {
        if (q != null && !q.isEmpty()) {
            return repository.search(q);
        }
        return repository.findAll();
    }

    public ClienteDto findById(Long id) {
        Cliente c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return toDto(c);
    }

    @Transactional
    public Cliente create(Cliente c) {
        return repository.save(c);
    }

    @Transactional
    public Cliente update(Long id, Cliente c) {
        Cliente existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        if (c.getNombre() != null) existing.setNombre(c.getNombre());
        if (c.getApellido() != null) existing.setApellido(c.getApellido());
        if (c.getEmail() != null) existing.setEmail(c.getEmail());
        if (c.getTelefono() != null) existing.setTelefono(c.getTelefono());
        if (c.getDireccion() != null) existing.setDireccion(c.getDireccion());
        if (c.getIdentificacionFiscal() != null) existing.setIdentificacionFiscal(c.getIdentificacionFiscal());
        if (c.getFechaNacimiento() != null) existing.setFechaNacimiento(c.getFechaNacimiento());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public ClienteDto toDto(Cliente c) {
        ClienteDto dto = new ClienteDto();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        dto.setApellido(c.getApellido());
        dto.setFechaNacimiento(c.getFechaNacimiento());
        dto.setEmail(c.getEmail());
        dto.setTelefono(c.getTelefono());
        dto.setDireccion(c.getDireccion());
        dto.setIdentificacionFiscal(c.getIdentificacionFiscal());
        dto.setPortalActivo(c.getPortalActivo());
        dto.setMascotas(c.getMascotas().stream().map(m -> {
            MascotaDto md = new MascotaDto();
            md.setId(m.getId());
            md.setNombre(m.getNombre());
            md.setEspecie(m.getEspecie().name());
            md.setRaza(m.getRaza());
            md.setColor(m.getColor());
            md.setGenero(m.getGenero() != null ? m.getGenero().name() : null);
            md.setFechaNacimiento(m.getFechaNacimiento());
            md.setPeso(m.getPeso());
            return md;
        }).toList());
        return dto;
    }
}
