package com.veterinary.service;

import com.veterinary.domain.Veterinario;
import com.veterinary.domain.enums.RolUsuario;
import com.veterinary.repository.VeterinarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class VeterinarioService {

    private final VeterinarioRepository repository;
    private final PasswordEncoder encoder;

    public VeterinarioService(VeterinarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public List<Veterinario> findAll() {
        return repository.findByActivoTrue();
    }

    public Veterinario findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
    }

    @Transactional
    public Veterinario create(Veterinario v) {
        if (repository.existsByEmail(v.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        v.setPasswordHash(encoder.encode(v.getPasswordHash()));
        if (v.getRol() == null) v.setRol(RolUsuario.VETERINARIO);
        v.setActivo(true);
        return repository.save(v);
    }

    @Transactional
    public Veterinario update(Long id, Veterinario v) {
        Veterinario existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
        if (v.getNombre() != null) existing.setNombre(v.getNombre());
        if (v.getApellido() != null) existing.setApellido(v.getApellido());
        if (v.getTelefono() != null) existing.setTelefono(v.getTelefono());
        if (v.getEspecialidad() != null) existing.setEspecialidad(v.getEspecialidad());
        if (v.getCedulaProfesional() != null) existing.setCedulaProfesional(v.getCedulaProfesional());
        if (v.getHorarioInicio() != null) existing.setHorarioInicio(v.getHorarioInicio());
        if (v.getHorarioFin() != null) existing.setHorarioFin(v.getHorarioFin());
        if (v.getDuracionTurnoMinutos() != null) existing.setDuracionTurnoMinutos(v.getDuracionTurnoMinutos());
        if (v.getRol() != null) existing.setRol(v.getRol());
        if (v.getPasswordHash() != null && !v.getPasswordHash().isEmpty()) {
            existing.setPasswordHash(encoder.encode(v.getPasswordHash()));
        }
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Veterinario v = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
        v.setActivo(false);
        repository.save(v);
    }

    @Transactional
    public Veterinario updateProfile(Long id, Map<String, String> body) {
        Veterinario v = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
        if (body.containsKey("nombre")) v.setNombre(body.get("nombre"));
        if (body.containsKey("apellido")) v.setApellido(body.get("apellido"));
        if (body.containsKey("telefono")) v.setTelefono(body.get("telefono"));
        return repository.save(v);
    }

    @Transactional
    public void changePassword(Long id, String actual, String nueva) {
        Veterinario v = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));
        if (!encoder.matches(actual, v.getPasswordHash())) {
            throw new RuntimeException("La contraseña actual no es correcta");
        }
        v.setPasswordHash(encoder.encode(nueva));
        repository.save(v);
    }
}
