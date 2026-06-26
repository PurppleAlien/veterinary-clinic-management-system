package com.veterinary.repository;

import com.veterinary.domain.Cita;
import com.veterinary.domain.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByVeterinarioIdOrderByFechaHoraInicioDesc(Long veterinarioId);
    List<Cita> findByClienteIdOrderByFechaHoraInicioDesc(Long clienteId);
    List<Cita> findByMascotaIdOrderByFechaHoraInicioDesc(Long mascotaId);
    List<Cita> findByEstado(EstadoCita estado);

    @Query("SELECT c FROM Cita c WHERE c.veterinario.id = :veterinarioId AND c.fechaHoraInicio BETWEEN :inicio AND :fin ORDER BY c.fechaHoraInicio")
    List<Cita> findByVeterinarioAndFechaBetween(@Param("veterinarioId") Long veterinarioId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT c FROM Cita c WHERE c.veterinario.id = :veterinarioId AND c.fechaHoraInicio >= :inicio AND c.fechaHoraInicio < :fin AND c.estado NOT IN ('CANCELADA', 'NO_ASISTIO')")
    List<Cita> findCitasOcupadas(@Param("veterinarioId") Long veterinarioId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT c FROM Cita c WHERE c.fechaHoraInicio BETWEEN :inicio AND :fin ORDER BY c.fechaHoraInicio")
    List<Cita> findCitasDelDia(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(c) FROM Cita c WHERE c.veterinario.id = :veterinarioId AND c.fechaHoraInicio >= :inicio AND c.fechaHoraInicio < :fin")
    long countByVeterinarioAndFecha(@Param("veterinarioId") Long veterinarioId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
