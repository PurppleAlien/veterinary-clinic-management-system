package com.veterinary.service;

import com.veterinary.domain.ConsentimientoInformado;
import com.veterinary.repository.ClienteRepository;
import com.veterinary.repository.ConsentimientoRepository;
import com.veterinary.repository.VeterinarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsentimientoService {

    private final ConsentimientoRepository repository;
    private final ClienteRepository clienteRepository;
    private final VeterinarioRepository veterinarioRepository;

    public ConsentimientoService(ConsentimientoRepository repository, ClienteRepository clienteRepository,
                                  VeterinarioRepository veterinarioRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    public List<ConsentimientoInformado> findAll() {
        return repository.findAll();
    }

    public List<ConsentimientoInformado> findByCliente(Long clienteId) {
        return repository.findByClienteIdOrderByCreatedAtDesc(clienteId);
    }

    public ConsentimientoInformado findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consentimiento no encontrado"));
    }

    @Transactional
    public ConsentimientoInformado create(ConsentimientoInformado c, Long vetId) {
        c.setCliente(clienteRepository.findById(c.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado")));
        c.setVeterinario(veterinarioRepository.findById(vetId)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado")));
        return repository.save(c);
    }

    @Transactional
    public ConsentimientoInformado update(Long id, ConsentimientoInformado c) {
        ConsentimientoInformado existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consentimiento no encontrado"));
        if (c.getContenido() != null) existing.setContenido(c.getContenido());
        if (c.getTipoProcedimiento() != null) existing.setTipoProcedimiento(c.getTipoProcedimiento());
        return repository.save(existing);
    }

    @Transactional
    public ConsentimientoInformado sign(Long id, String nombreFirmante) {
        ConsentimientoInformado c = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consentimiento no encontrado"));
        c.setFirmado(true);
        c.setNombreFirmante(nombreFirmante);
        c.setFechaFirma(LocalDateTime.now());
        return repository.save(c);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
