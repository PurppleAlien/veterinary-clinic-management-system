package com.veterinary.repository;

import com.veterinary.domain.PasoTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasoTratamientoRepository extends JpaRepository<PasoTratamiento, Long> {
    List<PasoTratamiento> findByPlanTratamientoIdOrderByOrdenAsc(Long planId);
}
