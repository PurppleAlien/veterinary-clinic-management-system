package com.veterinary.repository;

import com.veterinary.domain.Odontograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OdontogramaRepository extends JpaRepository<Odontograma, Long> {
    List<Odontograma> findByMascotaIdOrderByFechaDesc(Long mascotaId);
}
