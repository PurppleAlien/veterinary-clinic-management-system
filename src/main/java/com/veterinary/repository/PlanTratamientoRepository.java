package com.veterinary.repository;

import com.veterinary.domain.PlanTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanTratamientoRepository extends JpaRepository<PlanTratamiento, Long> {
    List<PlanTratamiento> findByMascotaIdOrderByFechaInicioDesc(Long mascotaId);
}
