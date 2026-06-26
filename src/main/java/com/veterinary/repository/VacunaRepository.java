package com.veterinary.repository;

import com.veterinary.domain.Vacuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacunaRepository extends JpaRepository<Vacuna, Long> {
    List<Vacuna> findByMascotaIdOrderByFechaAplicacionDesc(Long mascotaId);
    List<Vacuna> findByFechaProximaDosisBetween(LocalDate inicio, LocalDate fin);
}
