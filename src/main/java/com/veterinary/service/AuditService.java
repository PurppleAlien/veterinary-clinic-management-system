package com.veterinary.service;

import com.veterinary.domain.AuditLog;
import com.veterinary.repository.AuditLogRepository;
import com.veterinary.domain.AuditLog;
import com.veterinary.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    public AuditService(AuditLogRepository auditLogRepository, HttpServletRequest request) {
        this.auditLogRepository = auditLogRepository;
        this.request = request;
    }

    public void log(String accion, String entidad, Long entidadId, String detalle, Long veterinarioId, String veterinarioEmail) {
        AuditLog log = new AuditLog();
        log.setAccion(accion);
        log.setEntidad(entidad);
        log.setEntidadId(entidadId);
        log.setDetalle(detalle);
        log.setVeterinarioId(veterinarioId);
        log.setVeterinarioEmail(veterinarioEmail);

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        log.setIpAddress(ip);

        auditLogRepository.save(log);
    }

    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByFechaAccionDesc();
    }
}
