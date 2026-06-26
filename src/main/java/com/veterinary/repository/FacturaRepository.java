package com.veterinary.repository;

import com.veterinary.domain.Factura;
import com.veterinary.domain.enums.EstadoFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    List<Factura> findByClienteIdOrderByFechaEmisionDesc(Long clienteId);
    List<Factura> findByVeterinarioIdOrderByFechaEmisionDesc(Long veterinarioId);
    List<Factura> findByEstado(EstadoFactura estado);

    @Query("SELECT f FROM Factura f WHERE f.fechaEmision BETWEEN :inicio AND :fin ORDER BY f.fechaEmision")
    List<Factura> findByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(f.total), 0) FROM Factura f WHERE f.estado <> 'ANULADA' AND f.fechaEmision BETWEEN :inicio AND :fin")
    Double sumIngresosByFecha(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
