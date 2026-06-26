package com.veterinary.repository;

import com.veterinary.domain.Veterinario;
import com.veterinary.domain.enums.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {
    Optional<Veterinario> findByEmail(String email);
    List<Veterinario> findByRol(RolUsuario rol);
    List<Veterinario> findByActivoTrue();
    boolean existsByEmail(String email);
}
