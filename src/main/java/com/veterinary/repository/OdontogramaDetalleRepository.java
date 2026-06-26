package com.veterinary.repository;

import com.veterinary.domain.OdontogramaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OdontogramaDetalleRepository extends JpaRepository<OdontogramaDetalle, Long> {
    List<OdontogramaDetalle> findByOdontogramaId(Long odontogramaId);
}
