package com.veterinary.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accion;

    @Column(nullable = false)
    private String entidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(name = "veterinario_id")
    private Long veterinarioId;

    @Column(name = "veterinario_email")
    private String veterinarioEmail;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "fecha_accion")
    private LocalDateTime fechaAccion;

    @PrePersist
    protected void onCreate() {
        if (fechaAccion == null) {
            fechaAccion = LocalDateTime.now();
        }
    }
}
