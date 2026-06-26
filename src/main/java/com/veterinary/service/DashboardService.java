package com.veterinary.service;

import com.veterinary.domain.enums.EstadoCita;
import com.veterinary.domain.enums.EstadoHospitalizacion;
import com.veterinary.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final CitaRepository citaRepository;
    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ServicioRepository servicioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final HospitalizacionRepository hospitalizacionRepository;
    private final FacturaRepository facturaRepository;

    public DashboardService(CitaRepository citaRepository, ClienteRepository clienteRepository,
                             MascotaRepository mascotaRepository, VeterinarioRepository veterinarioRepository,
                             ServicioRepository servicioRepository, MedicamentoRepository medicamentoRepository,
                             HospitalizacionRepository hospitalizacionRepository, FacturaRepository facturaRepository) {
        this.citaRepository = citaRepository;
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.servicioRepository = servicioRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.hospitalizacionRepository = hospitalizacionRepository;
        this.facturaRepository = facturaRepository;
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClientes", clienteRepository.count());
        stats.put("totalMascotas", mascotaRepository.count());
        stats.put("totalVeterinarios", veterinarioRepository.count());
        stats.put("totalServicios", servicioRepository.count());
        stats.put("totalMedicamentos", medicamentoRepository.count());
        stats.put("citasHoy", citaRepository.findCitasDelDia(
                LocalDateTime.now().with(LocalTime.MIN),
                LocalDateTime.now().with(LocalTime.MAX)).size());
        stats.put("citasPendientes", citaRepository.findByEstado(EstadoCita.PROGRAMADA).size());
        stats.put("hospitalizados", hospitalizacionRepository.findByEstado(EstadoHospitalizacion.HOSPITALIZADO).size());
        stats.put("stockBajo", medicamentoRepository.findByStockActualLessThanEqual(5).size());
        return stats;
    }
}
