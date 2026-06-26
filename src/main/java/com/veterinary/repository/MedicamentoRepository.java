package com.veterinary.repository;

import com.veterinary.domain.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    List<Medicamento> findByActivoTrue();
    List<Medicamento> findByStockActualLessThanEqual(Integer stockMinimo);
}
