package com.veterinary.repository;

import com.veterinary.domain.Hospitalizacion;
import com.veterinary.domain.enums.EstadoHospitalizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalizacionRepository extends JpaRepository<Hospitalizacion, Long> {
    List<Hospitalizacion> findByMascotaId(Long mascotaId);
    List<Hospitalizacion> findByEstado(EstadoHospitalizacion estado);
}
