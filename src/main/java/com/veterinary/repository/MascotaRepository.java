package com.veterinary.repository;

import com.veterinary.domain.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByClienteId(Long clienteId);
    List<Mascota> findByClienteIdOrderByNombreAsc(Long clienteId);
}
