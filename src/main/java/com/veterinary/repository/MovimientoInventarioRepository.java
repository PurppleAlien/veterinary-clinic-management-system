package com.veterinary.repository;

import com.veterinary.domain.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByMedicamentoIdOrderByFechaMovimientoDesc(Long medicamentoId);
}
