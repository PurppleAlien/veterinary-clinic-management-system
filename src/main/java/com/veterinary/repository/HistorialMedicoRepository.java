package com.veterinary.repository;

import com.veterinary.domain.HistorialMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialMedicoRepository extends JpaRepository<HistorialMedico, Long> {
    List<HistorialMedico> findByMascotaIdOrderByFechaConsultaDesc(Long mascotaId);
    List<HistorialMedico> findByCitaId(Long citaId);
}
