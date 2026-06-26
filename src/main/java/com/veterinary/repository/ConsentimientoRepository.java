package com.veterinary.repository;

import com.veterinary.domain.ConsentimientoInformado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsentimientoRepository extends JpaRepository<ConsentimientoInformado, Long> {
    List<ConsentimientoInformado> findByClienteIdOrderByCreatedAtDesc(Long clienteId);
    List<ConsentimientoInformado> findByVeterinarioIdOrderByCreatedAtDesc(Long veterinarioId);
}
